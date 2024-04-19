use std::collections::{HashMap, HashSet};

use log::debug;

// iterative method uses a stack that is in the heap, not the call stack
// depth limit 128 is used as flag to do random search without any depth limit
pub fn depth_limited_dfs(graph: &HashMap<&String, HashSet<&String>>, start_node: &String, target_node: &String, depth_limit: i8) -> Vec<String> {
    let mut depth: i8 = 0;
    let mut route_stack: Vec<String> = vec![];
    let mut search_stack: Vec<&String> = vec![start_node];
    let mut visited: HashSet<&String> = HashSet::new();
    let graph_level_keeper = "ALI_OZAN".to_string();

    // search_stack is imitation of call stack to have recursive algorithm behaviour
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

            // depth limit 128 is used as flag to do random search, so no need to clear visited nodes
            if depth_limit != i8::MAX {
                visited.clear();
            }
        } else {

            // depth limit controls the allowed levels to go down from starting node
            // if we are too deep, we should stop and go back to previous level to check remaining adjacency of parent
            if depth >= depth_limit {
                debug!("Cannot expand adjacency since depth ({}) is over limit!", depth);
                continue;
            }

            // going down one more level by expanding adjacent nodes
            depth += 1;
            route_stack.push(current_node.clone());

            // we will put a keeper to understand when we came back from this current depth level
            search_stack.push(&graph_level_keeper);

            let adjacency = graph.get(current_node).expect("Adjacency list of current node is not found in graph!");
            for border in adjacency {
                let border_ref = *border;

                // no need to get node from search stack to see if it is visited in next iteration
                // since here we do not even push previously visited nodes to search stack
                if !visited.contains(border_ref) {
                    search_stack.push(border_ref);
                }
            }
        }
    }

    // return empty route if not found
    vec![]
}
