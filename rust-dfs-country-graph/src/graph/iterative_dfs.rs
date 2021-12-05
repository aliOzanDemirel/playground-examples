use std::collections::{HashMap, HashSet};
use std::time::{Duration, Instant};

use actix_web::{HttpResponse, Responder, web};
use actix_web::http::StatusCode;
use log::{debug, error, info};

use crate::graph::load_countries::Country;

pub fn find_shortest_route(countries: &Vec<Country>, source: &String, destination: &String) -> (Vec<String>, Duration) {
    let countries_graph = create_graph_as_adjacency_list(&countries);
    let now = Instant::now();
    let route = iterative_deepening_dfs(&countries_graph, &source, &destination);
    (route, now.elapsed())
}

fn create_graph_as_adjacency_list(countries: &Vec<Country>) -> HashMap<&String, HashSet<&String>> {

    // adjacency list is HashSet because of short circuit check to see if target is in adjacent nodes
    let mut graph: HashMap<&String, HashSet<&String>> = HashMap::new();
    for country in countries {
        let country_code = &country.country_code;
        let country_borders = country.borders.iter().collect::<HashSet<&String>>();
        graph.insert(country_code, country_borders);
    }
    graph
}

fn iterative_deepening_dfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String) -> Vec<String> {

    // TODO: 10 is already too deep? takes too long but need even more depth, for example RUS -> ZAF or ZAF -> KHM
    let max_depth_limit: i8 = 10;
    for depth_limit in 0..=max_depth_limit {
        info!("Searching target: {} with depth limit: {}, starting from: {}", target_node, depth_limit, start_node);
        let now = Instant::now();
        let route = depth_limited_dfs(graph, start_node, target_node, depth_limit);
        let duration = now.elapsed();
        info!("Finished search with depth limit: {}, took {} micros, {} millis, {} secs",
            depth_limit, duration.as_micros(), duration.as_millis(), duration.as_secs());
        if !route.is_empty() {
            return route;
        }
    }
    vec![]
}

fn depth_limited_dfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String, depth_limit: i8) -> Vec<String> {
    let mut depth: i8 = 0;
    let mut route_stack: Vec<String> = vec![];
    let mut search_stack: Vec<&String> = vec![start_node];
    let mut visited: HashSet<&String> = HashSet::new();
    let graph_level_keeper = "ALI_OZAN".to_string();

    // iterative deepening depth first search
    while !search_stack.is_empty() {

        // current_node is actually the country in this specific graph
        let current_node = search_stack.pop().expect("No node exists in search stack!");

        // visited means we checked if this node is the target, to not check it again in a possibly infinite graph
        visited.insert(current_node);
        debug!("Currently visiting node: {}, route so far: {:?}", current_node, route_stack);

        if current_node.eq(target_node) {
            // succeeds finding target, complete route and return
            route_stack.push(current_node.clone());
            return route_stack;
        } else if current_node.eq(&graph_level_keeper) {

            // gets back to a previously visited graph level, so another level can be checked
            depth -= 1;

            // data under this keeper is actually the path taken to come to this level
            route_stack.pop();

            // no need to clear visited nodes if we mark nodes as visited only if they are expanded
            visited.clear();
        } else {

            // depth limit controls the allowed levels to go down from starting node.
            // if we are too deep, we should stop and go back to previous level to check remaining adjacency of parent
            if depth >= depth_limit {
                debug!("Cannot expand adjacency since depth ({:?}) is over limit!", depth);
                continue;
            }

            // going down one more level by expanding adjacent nodes
            // we will put a keeper to understand when we came back from this depth level
            depth += 1;
            search_stack.push(&graph_level_keeper);
            route_stack.push(current_node.clone());

            let adjacent = graph.get(current_node).expect("Adjacency list of current node is not found in graph!");

            // short circuit before pushing adjacent nodes to search stack
            if adjacent.contains(target_node) {
                // succeeds finding target, complete route and return
                route_stack.push(target_node.clone());
                return route_stack;
            }

            debug!("Short circuit failed, going down another level by expanding adjacent nodes: {:?}", adjacent);
            for border in adjacent {

                // no need to get node from search stack to see if it is visited in next iteration
                // since here we do not even push previously visited nodes to search stack
                if !visited.contains(border) {
                    search_stack.push(border);
                }
            }
        }
    }
    // return empty route if not found
    vec![]
}
