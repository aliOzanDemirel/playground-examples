use actix_web::{HttpRequest, HttpResponse, Responder};
use log::info;
use serde::Serialize;

// aliasing module
use super::super::mutexes_arcs_and_threads as mutex_module;

#[derive(Serialize)]
struct MutexResponse {
    requested_count: i8,
    counter_guarded_by_mutex: i16,
    counter_guarded_by_rwlock: i16,
}

pub async fn handle(req: HttpRequest) -> impl Responder {
    let iteration_count = match req.match_info().get("count") {
        Some(count) => count.parse::<i8>().unwrap_or(mutex_module::DEFAULT_COUNT),
        None => mutex_module::DEFAULT_COUNT
    };
    info!("Requested iteration_count: {} for mutex counter", iteration_count);

    let shared_counter = mutex_module::count_concurrently(iteration_count);
    let mutex = *shared_counter.mutex_counter.lock().unwrap();
    let rwlock = *shared_counter.rwlock_counter.read().unwrap();

    HttpResponse::Ok().json(MutexResponse {
        requested_count: iteration_count,
        counter_guarded_by_mutex: mutex,
        counter_guarded_by_rwlock: rwlock,
    })
}
