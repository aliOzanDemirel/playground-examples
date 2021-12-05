use std::collections::{HashMap, HashSet};
use std::time::{Duration, Instant};

use actix_web::{HttpResponse, Responder, web};
use actix_web::http::StatusCode;
use log::{debug, error, info};

use crate::graph::load_countries::Country;

pub fn find_route(countries: &Vec<Country>, source: &String, destination: &String) -> (Vec<String>, Duration) {
    let countries_graph: HashMap<&String, HashSet<&String>> = countries.iter()
        .map(|it| (&it.country_code, it.borders.iter().collect::<HashSet<&String>>()))
        .collect();

    let now = Instant::now();
    let visited_memory = &mut HashSet::new();
    let mut route = recursive_dfs(&countries_graph, &source, &destination, visited_memory);
    if !route.is_empty() {
        route.push(source.clone());
        route.reverse();
    }
    (route, now.elapsed())
}

pub fn iterative_deepening(countries: &Vec<Country>, source: &String, destination: &String) -> (Vec<String>, Duration) {
    let countries_graph: HashMap<&String, HashSet<&String>> = countries.iter()
        .map(|it| (&it.country_code, it.borders.iter().collect::<HashSet<&String>>()))
        .collect();

    let now = Instant::now();

    // depth level 0 case, checking for source as root
    if countries_graph.contains_key(source) && source.eq(destination) {
        return (vec![source.clone()], now.elapsed());
    }

    let max_depth_limit = i8::MAX;
    for depth_limit in 1..=max_depth_limit {
        info!("Searching target: {} with depth limit: {}, starting from: {}", destination, depth_limit, source);
        let depth_now = Instant::now();
        let visited_memory = &mut HashSet::new();
        let mut route = recursive_dl_dfs(&countries_graph, &source, &destination, visited_memory, 1, depth_limit);
        let depth_duration = now.elapsed();
        info!("Finished search with depth limit: {}, took {} micros, {} millis, {} secs",
            depth_limit, depth_duration.as_micros(), depth_duration.as_millis(), depth_duration.as_secs());
        if !route.is_empty() {
            route.push(source.clone());
            route.reverse();
            return (route, now.elapsed());
        }
    }
    (vec![], now.elapsed())
}


fn recursive_dl_dfs(graph: &HashMap<&String, HashSet<&String>>, current_node: &String, target_node: &String, visited: &mut HashSet<String>,
                    depth: i8, depth_limit: i8) -> Vec<String> {

    // this function looks for goal not in the node but the adjacency of node, so depth 0 is root, finding goal in depth 1
    // depth 1 is adjacent of root, finding goal in depth 2, and so on...

    visited.insert(current_node.clone());
    let adjacency = graph.get(current_node).expect("Node does not exist in graph!");
    // info!("VISITING current_node: {} | visited so far: {:?}", current_node, visited);

    if adjacency.contains(target_node) {
        return vec![target_node.clone()];
    } else {

        // do not check adjacency and expand more if limit is exceeded
        if depth >= depth_limit {
            return vec![];
        }

        for border in adjacency {
            let border_ref = *border;
            // info!("VISITING border: {} of current_node: {}", border, current_node);

            // do not expand one more level with recursion if border is already visited
            if visited.contains(border_ref) {
                continue;
            }

            let mut route = recursive_dl_dfs(&graph, border_ref, target_node, visited, depth + 1, depth_limit);
            // let mut route = recursive_dfs(&graph, adjacent_border, target_node, visited);
            if !route.is_empty() {
                route.push(border_ref.clone());
                return route;
            }
        }
    }
    vec![]
}

fn recursive_dfs(graph: &HashMap<&String, HashSet<&String>>, current_node: &String, target_node: &String, visited: &mut HashSet<String>) -> Vec<String> {
    visited.insert(current_node.clone());
    let adjacency = graph.get(current_node).expect("Node does not exist in graph!");

    if adjacency.contains(target_node) {
        return vec![target_node.clone()];
    } else {
        for border in adjacency {
            let adjacent_border = *border;

            // do not expand one more level with recursion if border is already visited
            if visited.contains(adjacent_border) {
                continue;
            }

            let mut route = recursive_dfs(&graph, adjacent_border, target_node, visited);
            if !route.is_empty() {
                route.push(adjacent_border.clone());
                return route;
            }
        }
    }
    vec![]
}