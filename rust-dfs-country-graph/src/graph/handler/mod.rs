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

pub async fn find_shortest_route(web::Path((source, destination, algorithm)): web::Path<(String, String, String)>) -> HttpResponse {
    let countries_result = super::load_country_graph::fetch_countries().await;
    let countries = match countries_result {
        Ok(c) => c,
        Err(e) => {
            error!("Could not load the list of countries with borders! Error: {}", e);
            return HttpResponse::from(e).into();
        }
    };

    // use hashset as adjacency list since it is used to check target is in borders
    let countries_graph: HashMap<&String, HashSet<&String>> = countries.iter()
        .map(|it| (&it.country_code, it.borders.iter().collect::<HashSet<&String>>()))
        .collect();

    // random path is the first possible result found -> source -> A -> B -> C -> target
    // shortest path is the path with least possible nodes, graph has no heuristics at the edges -> source -> C -> target
    let (route, search_duration, found_depth) = match algorithm.as_str() {
        "DfsShortestRecursive" =>
            super::dfs::iterative_deepening_dl_dfs(false, &countries_graph, &source, &destination),
        "DfsRandomRecursive" =>
            super::dfs::random_path_dfs(false, &countries_graph, &source, &destination),
        "DfsShortestIterative" =>
            super::dfs::iterative_deepening_dl_dfs(true, &countries_graph, &source, &destination),
        "DfsRandomIterative" =>
            super::dfs::random_path_dfs(true, &countries_graph, &source, &destination),
        "BfsShortestIterative" =>
            super::bfs::bfs(&countries_graph, &source, &destination),
        _ => return HttpResponse::BadRequest().finish()
    };

    let response = HashMap::from([
        ("route", route.join(" > ")),
        // depth_limit is 0 indexed, it is the distance to the target from the start node
        // it will be -1 if not applicable to random dfs algorithms and not implemented for bfs
        ("depth_limit", found_depth.to_string()),
        ("micros", search_duration.as_micros().to_string()),
        ("millis", search_duration.as_millis().to_string()),
        ("secs", search_duration.as_secs().to_string())
    ]);

    if route.is_empty() {
        HttpResponse::NotFound().json(response).into()
    } else {
        HttpResponse::Ok().json(response).into()
    }
}