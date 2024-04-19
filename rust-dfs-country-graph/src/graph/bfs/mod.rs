use std::collections::{HashMap, HashSet, VecDeque};
use std::time::{Duration, Instant};

use log::info;

// search the shortest path from start to target node
pub fn bfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String) -> (Vec<String>, Duration, i8) {
    info!("Starting to search shortest path from '{}' to '{}' with breadth-first search, bfs implementation: Iterative",
        start_node, target_node);

    let now = Instant::now();
    let route = shortest_path_bfs(graph, start_node, target_node);
    (route, now.elapsed(), -1)
}

fn shortest_path_bfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String) -> Vec<String> {
    let mut search_queue: VecDeque<Vec<String>> = VecDeque::new();
    search_queue.push_back(vec![start_node.clone()]);

    while !search_queue.is_empty() {
        let current_node_path = search_queue.pop_front().unwrap();
        let current_node = current_node_path.last().unwrap();

        if current_node.eq(target_node) {

            // succeeds finding target, complete route and return
            return current_node_path;
        } else {
            let adjacent = graph.get(current_node).expect("Adjacency list of current node is not found in graph!");
            for border in adjacent {
                let border_ref = *border;

                let mut cop = current_node_path.clone();
                cop.push(border_ref.clone());
                search_queue.push_back(cop);
            }
        }
    }

    // return empty route if not found
    vec![]
}
