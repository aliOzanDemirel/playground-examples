use std::collections::HashMap;

use actix_web::{HttpResponse, Responder, web};

// lifetime 'static is implicit for constant literals
pub const ROOT_PATH_LEARNING: &str = "/learning";
const PATH_PRIMITIVES: &str = "primitives_and_strings";
const PATH_TUPLES: &str = "tuples_and_arrays";
const PATH_STRUCTS: &str = "structs_and_enums";
const PATH_LOOPS: &str = "loops_and_iterators";
const PATH_BOXES: &str = "boxes_and_options";
const PATH_METHODS: &str = "methods_and_closures";
const PATH_MUTEX: &str = "mutexes_arcs_and_threads";

// not supported to concatenate two constant string literals, so a custom macro is needed to initialize this constant array
lazy_static! {
    static ref LEARNING_PATHS: [String; 7] = [
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_PRIMITIVES),
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_TUPLES),
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_STRUCTS),
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_LOOPS),
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_BOXES),
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_METHODS),
        format!("{}/{}", ROOT_PATH_LEARNING, PATH_MUTEX),
    ];
}

pub async fn learning_subjects() -> impl Responder {
    let mut response_json = HashMap::new();
    response_json.insert("learning_subjects", &*LEARNING_PATHS);
    HttpResponse::Ok().json(response_json)
}

pub async fn learning_code(web::Path(learning_path): web::Path<String>) -> impl Responder {
    let example_code = match learning_path.as_str() {
        PATH_PRIMITIVES => {
            use super::super::primitives_and_strings as module;
            module::example();
            module::CODE
        }
        PATH_TUPLES => {
            use super::super::tuples_and_arrays as module;
            module::example();
            module::CODE
        }
        PATH_STRUCTS => {
            use super::super::structs_and_enums as module;
            module::example();
            module::CODE
        }
        PATH_LOOPS => {
            use super::super::loops_and_iterators as module;
            module::example();
            module::CODE
        }
        PATH_BOXES => {
            use super::super::boxes_and_options as module;
            module::example();
            module::CODE
        }
        PATH_METHODS => {
            use super::super::methods_and_closures as module;
            module::example();
            module::CODE
        }
        PATH_MUTEX => super::super::mutexes_arcs_and_threads::CODE,
        _ => "No example is found!"
    };

    let response_in_lines: Vec<&str> = example_code.lines().collect();
    HttpResponse::Ok().json(response_in_lines)
}