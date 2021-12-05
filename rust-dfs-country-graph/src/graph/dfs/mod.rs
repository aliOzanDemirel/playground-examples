use std::collections::{HashMap, HashSet};
use std::time::{Duration, Instant};

use log::info;

pub mod iterative;
pub mod recursive;

pub fn iterative_deepening_iterative_dl_dfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String) -> (Vec<String>, Duration, i8) {
    let now = Instant::now();

    info!("Starting to search for path: {} -> {} with iterative depth limited depth-first search", start_node, target_node);

    // TODO: 10 is already too deep? takes too long but need even more depth, for example RUS -> ZAF or ZAF -> KHM
    let max_depth_limit: i8 = 10;
    for depth_limit in 0..=max_depth_limit {
        info!("Searching with depth limit: {}", depth_limit);
        let depth_timer = Instant::now();
        let route = iterative::depth_limited_dfs(graph, start_node, target_node, depth_limit);
        let depth_duration = depth_timer.elapsed();
        info!("Finished search with depth limit: {}, took {} micros, {} millis, {} secs",
            depth_limit, depth_duration.as_micros(), depth_duration.as_millis(), depth_duration.as_secs());

        // if goal is found in this depth level iteration
        if !route.is_empty() {
            return (route, now.elapsed(), depth_limit);
        }
    }
    (vec![], now.elapsed(), -1)
}

pub fn iterative_deepening_recursive_dl_dfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String) -> (Vec<String>, Duration, i8) {
    let now = Instant::now();

    info!("Starting to search for path: {} -> {} with recursive depth limited depth-first search", start_node, target_node);

    let max_depth_limit = i8::MAX;
    for depth_limit in 0..=max_depth_limit {
        info!("Searching with depth limit: {}", depth_limit);
        let depth_timer = Instant::now();
        let route = recursive::depth_limited_dfs(&graph, &start_node, &target_node, depth_limit);
        let depth_duration = depth_timer.elapsed();
        info!("Finished search with depth limit: {}, took {} micros, {} millis, {} secs",
            depth_limit, depth_duration.as_micros(), depth_duration.as_millis(), depth_duration.as_secs());

        // if goal is found in this depth level iteration
        if !route.is_empty() {
            return (route, now.elapsed(), depth_limit);
        }
    }
    (vec![], now.elapsed(), -1)
}

pub fn iterative_dfs_random_path(graph: &HashMap<&String, HashSet<&String>>, source: &String, destination: &String) -> (Vec<String>, Duration, i8) {
    let now = Instant::now();
    let route = iterative::depth_limited_dfs(graph, source, destination, i8::MAX);
    (route, now.elapsed(), i8::MAX)
}

// random leaf search recursively
pub fn recursive_dfs_random_path(graph: &HashMap<&String, HashSet<&String>>, source: &String, destination: &String) -> (Vec<String>, Duration, i8) {
    let now = Instant::now();
    let route = recursive::depth_limited_dfs(graph, source, destination, i8::MAX);
    (route, now.elapsed(), i8::MAX)
}