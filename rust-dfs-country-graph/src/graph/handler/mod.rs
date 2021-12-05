use std::collections::HashMap;
use std::time::Instant;

use actix_web::{HttpResponse, Responder, web};
use actix_web::http::StatusCode;
use log::{debug, error, info};

pub async fn list_country_borders() -> impl Responder {
    let countries_result = super::load_countries::fetch_countries().await;
    let countries = match countries_result {
        Ok(c) => c,
        Err(e) => {
            // better to use actix_web::Error
            error!("Could not fetch the list of countries, error: {}", e);
            return HttpResponse::new(StatusCode::INTERNAL_SERVER_ERROR);
        }
    };

    let countries_string: Vec<String> = countries.iter().map(|it| it.to_string()).collect();
    debug!("Fetched countries: {:?}", countries_string);

    HttpResponse::Ok().json(countries)
}

pub async fn find_shortest_route(web::Path((source, destination)): web::Path<(String, String)>) -> impl Responder {
    let countries_result = super::load_countries::fetch_countries().await;
    let countries = match countries_result {
        Ok(c) => c,
        Err(e) => {
            error!("Could not fetch the list of countries, error: {}", e);
            return HttpResponse::InternalServerError().finish();
        }
    };

    // let (route, dfs_duration) = super::recursive_dfs::iterative_deepening(&countries, &source, &destination);
    let (route, dfs_duration) = super::iterative_dfs::find_shortest_route(&countries, &source, &destination);
    let response = HashMap::from([
        ("route", route.join(" > ")),
        ("micros", dfs_duration.as_micros().to_string()),
        ("millis", dfs_duration.as_millis().to_string()),
        ("secs", dfs_duration.as_secs().to_string())
    ]);

    if route.is_empty() {
        HttpResponse::NotFound().json(response)
    } else {
        HttpResponse::Ok().json(response)
    }
}