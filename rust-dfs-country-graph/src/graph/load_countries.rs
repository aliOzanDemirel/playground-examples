use core::fmt;
use std::fs;

use actix_web::client::Client;
use actix_web::web::Buf;
use log::{debug, error, info};
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

pub async fn fetch_countries() -> Result<Vec<Country>, Box<dyn std::error::Error>> {
    let http_client = Client::default();
    let mut response = http_client
        .get("https://raw.githubusercontent.com/mledoze/countries/master/countries.json")
        .header("User-Agent", "actix-web, iterative deepening depth-first search")
        .send()
        .await?;
    debug!("Received response: {:?}", response);

    // read all response body as bytes
    let response_body = response.body().limit(TWO_MB_IN_BYTES).await?;

    // deserialize raw bytes to json
    let countries: Vec<Country> = serde_json::from_slice(response_body.bytes())?;
    debug!("Response body is deserialized to json list of {} items", countries.len());

    Ok(countries)
}

pub fn read_countries_file() -> Result<Vec<Country>, Box<dyn std::error::Error>> {
    let file = fs::File::open("countries_and_borders.json").expect("File 'countries_and_borders.json' could not be opened!");
    let countries: Vec<Country> = serde_json::from_reader(file).expect("File 'countries_and_borders.json' is not valid JSON!");
    debug!("File content is deserialized to json list of {} items", countries.len());

    Ok(countries)
}