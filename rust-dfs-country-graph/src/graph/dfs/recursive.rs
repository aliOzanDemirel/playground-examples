use std::collections::{HashMap, HashSet};

use log::debug;

// TODO: visited nodes are not cleared properly? does not really find the shortest yet
pub fn depth_limited_dfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String, depth_limit: i8) -> Vec<String> {

    // case for when depth level is 0, checking if root node is the goal
    // all other levels are processed by checking adjacency list of node, recursively
    if graph.contains_key(start_node) && start_node.eq(target_node) {
        return vec![start_node.clone()];
    }

    let visited = &mut HashSet::new();
    let mut route = recursive_dl_dfs(&graph, &start_node, &target_node, visited, 1, depth_limit);
    if !route.is_empty() {
        route.push(start_node.clone());
        route.reverse();
    }
    route
}

fn recursive_dl_dfs(graph: &HashMap<&String, HashSet<&String>>, current_node: &String, target_node: &String,
                    visited: &mut HashSet<String>, depth: i8, depth_limit: i8) -> Vec<String> {
    visited.insert(current_node.clone());
    debug!("Currently visiting node: {}, visited so far: {:?}", current_node, visited);

    let adjacency = graph.get(current_node).expect("Node does not exist in graph!");
    if adjacency.contains(target_node) {
        return vec![target_node.clone()];
    }

    // do not check adjacency and expand more if limit is exceeded
    if depth >= depth_limit {
        debug!("Cannot expand adjacency since depth ({}) is over limit!", depth);
        return vec![];
    }

    let next_depth = depth + 1;
    for border in adjacency {
        let border_ref = *border;

        // do not expand one more level with recursion if border is already visited
        if visited.contains(border_ref) {
            continue;
        }

        let mut route = recursive_dl_dfs(&graph, border_ref, target_node, visited, next_depth, depth_limit);
        if !route.is_empty() {
            route.push(border_ref.clone());
            return route;
        }
    }
    vec![]
}
