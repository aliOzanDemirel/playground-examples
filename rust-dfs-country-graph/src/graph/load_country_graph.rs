use core::fmt;
use std::fs;

use actix_web::client::Client;
use actix_web::Error;
use actix_web::web::{Buf, Bytes};
use log::{debug, error};
use serde::{Deserialize, Serialize};

const TWO_MB_IN_BYTES: usize = 2_097_152;

// only necessary fields are kept in memory
#[derive(Serialize, Deserialize)]
pub struct Country {
    #[serde(rename = "cca3")]
    pub country_code: String,
    pub borders: Vec<String>,
}

impl fmt::Display for Country {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Country: {} | borders: {}", self.country_code, self.borders.join(", "))
    }
}

pub async fn fetch_countries() -> Result<Vec<Country>, Error> {
    let http_client = Client::default();

    // get response from http call
    let response_body_result = http_client
        .get("https://raw.githubusercontent.com/mledoze/countries/master/countries.json")
        .header("User-Agent", "actix-web, depth-first and breadth-first search")
        .send()
        .await;

    // read response body as bytes if call is successful, or read from file
    match response_body_result {
        Ok(mut response_body) => {
            let response_bytes = response_body.body()
                .limit(TWO_MB_IN_BYTES)
                .await
                .unwrap();
            read_countries_response_body(response_bytes)
        }
        Err(error) => {
            error!("Could not fetch the countries! Error: {:?} Loading from local file.", error);
            read_countries_file("countries_and_borders.json")
        }
    }
}

// deserialize raw bytes of http response to json list of countries
fn read_countries_response_body(response_body: Bytes) -> Result<Vec<Country>, Error> {
    let countries: Result<Vec<Country>, Error> = serde_json::from_slice(response_body.bytes())
        .map_err(|it| Error::from(it));
    debug!("Response body is tried to be deserialized, with success: {}", countries.is_ok());
    countries
}

// deserialize fallback json file's content to list of countries
fn read_countries_file(file_name: &str) -> Result<Vec<Country>, Error> {
    let file = fs::File::open(file_name)
        .map_err(|it| Error::from(it))?;
    let countries = serde_json::from_reader(file)
        .map_err(|it| Error::from(it));
    debug!("File content is tried to be deserialized, with success: {}", countries.is_ok());
    countries
}

#[cfg(test)]
mod tests {
    use super::*;

    // #[actix_rt::test]
    #[test]
    fn read_fallback_file() {
        let result = read_countries_file("countries_and_borders.json");
        assert_eq!(result.is_ok(), true);
        assert_eq!(result.unwrap().is_empty(), false);
    }
}