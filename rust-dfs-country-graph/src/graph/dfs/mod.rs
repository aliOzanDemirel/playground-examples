use std::collections::{HashMap, HashSet};
use std::time::{Duration, Instant};

use log::info;

mod iterative;
mod recursive;

// iterative deepening method will always find the shortest path in this unweighted graph
pub fn iterative_deepening_dl_dfs(is_iterative_dfs: bool, graph: &HashMap<&String, HashSet<&String>>,
                                  start_node: &String, target_node: &String) -> (Vec<String>, Duration, i8) {
    info!("Starting to search shortest path from '{}' to '{}' with iterative deepening depth limited depth-first search, dfs implementation: {}",
        start_node, target_node, dfs_impl_name(is_iterative_dfs));

    let now = Instant::now();

    let max_depth_limit = i8::MAX - 1;
    for depth_limit in 0..=max_depth_limit {
        info!("Searching with depth limit: {}", depth_limit);
        let depth_timer = Instant::now();

        let route = if is_iterative_dfs {
            iterative::depth_limited_dfs(graph, start_node, target_node, depth_limit)
        } else {
            recursive::depth_limited_dfs(graph, start_node, target_node, depth_limit)
        };

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

// search any random path from start node to target node, not necessarily the shortest path
pub fn random_path_dfs(is_iterative_dfs: bool, graph: &HashMap<&String, HashSet<&String>>,
                       start_node: &String, target_node: &String) -> (Vec<String>, Duration, i8) {
    info!("Starting to search any random path from '{}' to '{}' with depth-first search, dfs implementation: {}",
        start_node, target_node, dfs_impl_name(is_iterative_dfs));

    let now = Instant::now();

    let max_depth_limit = i8::MAX;
    let route = if is_iterative_dfs {
        iterative::depth_limited_dfs(graph, start_node, target_node, max_depth_limit)
    } else {
        recursive::depth_limited_dfs(graph, start_node, target_node, max_depth_limit)
    };

    (route, now.elapsed(), -1)
}

fn dfs_impl_name(is_iterative_dfs: bool) -> &'static str {
    if is_iterative_dfs {
        "Iterative"
    } else {
        "Recursive"
    }
}