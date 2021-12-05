#[macro_use]
extern crate lazy_static;

use actix_web::{App, HttpServer, web};
use log::{error, info, LevelFilter};
use log4rs::append::console::ConsoleAppender;
use log4rs::Config;
use log4rs::config::{Appender, Root};

use learning::handler::learning_subjects as learn;

mod graph;
mod learning;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    configure_logging();
    HttpServer::new(|| {
        App::new().service(
            web::scope(learn::ROOT_PATH_LEARNING)
                .route("", web::get().to(learn::learning_subjects))
                .route("/", web::get().to(learn::learning_subjects))
                .route("/count/{count}", web::get().to(learning::handler::count_concurrent::handle))
                .route("/{learning_path}", web::get().to(learn::learning_code))
        ).service(
            web::scope("/graph")
                .route("/inspect", web::get().to(graph::handler::list_country_borders))
                .route("/routing/{source}/{destination}/{algorithm}", web::get().to(graph::handler::find_shortest_route))
        )
    })
        // actix server will have worker thread per virtual cpu, these workers should not be blocked
        .workers(num_cpus::get())
        .bind("127.0.0.1:8080")?
        .run()
        .await
}

fn configure_logging() {
    let stdout = ConsoleAppender::builder().build();
    let config = Config::builder()
        .appender(Appender::builder().build("stdout", Box::new(stdout)))
        .build(Root::builder().appender("stdout").build(LevelFilter::Info))
        .unwrap();
    let result = log4rs::init_config(config);
    match result {
        Ok(_) => info!("Configured logging to standard output"),
        Err(error) => error!("Error occurred while configuring logger: {}", error),
    }
}