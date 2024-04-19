pub mod handler;
mod load_country_graph;
mod dfs;
mod bfs;

// integration tests are written outside of the crate's source code, as if it is external client calling application code
#[cfg(test)]
mod tests {
    use actix_web::{http, web};

    use super::*;

    #[actix_rt::test]
    async fn dfs_random_recursive() {
        let path = web::Path(("ITA".to_string(), "CZE".to_string(), "DfsRandomRecursive".to_string()));

        let http_response = handler::find_shortest_route(path).await;
        assert_eq!(http_response.status(), http::StatusCode::OK);
    }

    #[actix_rt::test]
    async fn dfs_random_iterative() {
        let path = web::Path(("ITA".to_string(), "CZE".to_string(), "DfsRandomIterative".to_string()));

        let http_response = handler::find_shortest_route(path).await;
        assert_eq!(http_response.status(), http::StatusCode::OK);
    }

    // all tests for shortest algorithms can check the actual path found for ITA -> CZE since it is always same
    #[actix_rt::test]
    async fn dfs_shortest_recursive() {
        let path = web::Path(("ITA".to_string(), "CZE".to_string(), "DfsShortestRecursive".to_string()));

        let http_response = handler::find_shortest_route(path).await;
        assert_eq!(http_response.status(), http::StatusCode::OK);
    }

    #[actix_rt::test]
    async fn dfs_shortest_iterative() {
        let path = web::Path(("ITA".to_string(), "CZE".to_string(), "DfsShortestIterative".to_string()));

        let http_response = handler::find_shortest_route(path).await;
        assert_eq!(http_response.status(), http::StatusCode::OK);
    }

    #[actix_rt::test]
    async fn bfs_shortest_iterative() {
        let path = web::Path(("ITA".to_string(), "CZE".to_string(), "BfsShortestIterative".to_string()));

        let http_response = handler::find_shortest_route(path).await;
        assert_eq!(http_response.status(), http::StatusCode::OK);
    }
}