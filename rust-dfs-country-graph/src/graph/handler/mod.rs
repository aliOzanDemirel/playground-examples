use std::collections::{HashMap, HashSet};

use actix_web::{HttpResponse, Responder, web};
use log::{debug, error};

pub async fn list_country_borders() -> impl Responder {
    let countries_result = super::load_country_graph::fetch_countries().await;
    let countries = match countries_result {
        Ok(c) => c,
        Err(e) => {
            error!("Could not load the list of countries with borders! Error: {}", e);
            return HttpResponse::from(e);
        }
    };

    let countries_string: Vec<String> = countries.iter().map(|it| it.to_string()).collect();
    debug!("Countries: {:?}", countries_string);

    HttpResponse::Ok().json(countries)
}

pub async fn find_shortest_route(web::Path((source, destination, algorithm)): web::Path<(String, String, String)>) -> impl Responder {
    let countries_result = super::load_country_graph::fetch_countries().await;
    let countries = match countries_result {
        Ok(c) => c,
        Err(e) => {
            error!("Could not load the list of countries with borders! Error: {}", e);
            return HttpResponse::from(e);
        }
    };

    // let countries_graph: HashMap<&String, &Vec<String>> = countries.iter()
    //      .map(|it| (&it.country_code, &it.borders))
    //      .collect();
    let countries_graph: HashMap<&String, HashSet<&String>> = countries.iter()
        .map(|it| (&it.country_code, it.borders.iter().collect::<HashSet<&String>>()))
        .collect();

    let (route, search_duration, found_depth) = match algorithm.as_str() {
        "RecursiveShortest" => super::dfs::iterative_deepening_recursive_dl_dfs(&countries_graph, &source, &destination),
        "RecursiveRandom" => super::dfs::recursive_dfs_random_path(&countries_graph, &source, &destination),
        "IterativeShortest" => super::dfs::iterative_deepening_iterative_dl_dfs(&countries_graph, &source, &destination),
        "IterativeRandom" => super::dfs::iterative_dfs_random_path(&countries_graph, &source, &destination),
        _ => return HttpResponse::BadRequest().finish()
    };

    let response = HashMap::from([
        ("route", route.join(" > ")),
        // 0 indexed, distance of the target node from the start node
        ("depth_limit", found_depth.to_string()),
        ("micros", search_duration.as_micros().to_string()),
        ("millis", search_duration.as_millis().to_string()),
        ("secs", search_duration.as_secs().to_string())
    ]);

    if route.is_empty() {
        HttpResponse::NotFound().json(response)
    } else {
        HttpResponse::Ok().json(response)
    }
}