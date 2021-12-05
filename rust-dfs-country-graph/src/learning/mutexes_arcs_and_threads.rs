use std::sync::{Arc, Mutex, RwLock};
use std::thread;
use std::time::Duration;

pub struct SharedCounter {
    // guarded by mutex for concurrent access
    pub mutex_counter: Mutex<i16>,
    // guarded by read/write lock, better performance than mutex for read contention
    pub rwlock_counter: RwLock<i16>,
}

impl SharedCounter {
    fn new() -> SharedCounter {
        SharedCounter {
            mutex_counter: Mutex::new(0),
            rwlock_counter: RwLock::new(0),
        }
    }
}

pub const DEFAULT_COUNT: i8 = 10;

pub(crate) fn count_concurrently(requested_count: i8) -> Arc<SharedCounter> {
    let mut iteration_count = requested_count;
    if iteration_count < 1 {
        iteration_count = DEFAULT_COUNT
    }

    // reference counter (atomic for thread safety) wrapper to share reference to same data between
    // different threads but this shared access is read only, Mutex/MutexGuard provides write access
    let shared_counter: Arc<SharedCounter> = Arc::new(SharedCounter::new());

    for i in 0..iteration_count {

        // cloned the reference to same data to distribute ownership to different threads
        let shared_counter = Arc::clone(&shared_counter);
        let counter_thread_name = format!("Counter-{}", i);

        // ideally should use the returned handler to join spawned threads for structured concurrency
        thread::Builder::new().name(counter_thread_name)
            .spawn(|| {
                count(shared_counter);
            });
    }

    // dummy joining as simplest way for waiting multiple concurrent threads
    thread::sleep(Duration::from_secs(1));

    // no dereferencing smart pointer with '*' so that reference is copied
    shared_counter
}

fn count(shared_counter: Arc<SharedCounter>) {
    let current_thread = thread::current();
    let thread_name = current_thread.name().unwrap_or("Unassigned");

    // mutex does not give write access directly, it requires the lock() to be called
    // because only mutex guard implements DerefMut hence can give away mutable reference
    let mut mutex_counter_guard = shared_counter.mutex_counter.lock().unwrap();
    *mutex_counter_guard += 1;
    println!("Incremented mutex counter: {} in thread {}", mutex_counter_guard, thread_name);

    let mut rwlock_counter_guard = shared_counter.rwlock_counter.write().unwrap();
    *rwlock_counter_guard += 1;
    println!("Incremented rwlock counter: {} in thread {}", rwlock_counter_guard, thread_name);
}

pub const CODE: &'static str = r##"
use std::sync::{Arc, Mutex, RwLock};
use std::thread;
use std::time::Duration;

pub struct SharedCounter {
    // guarded by mutex for concurrent access
    pub mutex_counter: Mutex<i16>,
    // guarded by read/write lock, better performance than mutex for read contention
    pub rwlock_counter: RwLock<i16>,
}

impl SharedCounter {
    fn new() -> SharedCounter {
        SharedCounter {
            mutex_counter: Mutex::new(0),
            rwlock_counter: RwLock::new(0),
        }
    }
}

pub const DEFAULT_COUNT: i8 = 10;

pub(crate) fn count_concurrently(requested_count: i8) -> Arc<SharedCounter> {
    let mut iteration_count = requested_count;
    if iteration_count < 1 {
        iteration_count = DEFAULT_COUNT
    }

    // reference counter (atomic for thread safety) wrapper to share reference to same data between
    // different threads but this shared access is read only, Mutex/MutexGuard provides write access
    let shared_counter: Arc<SharedCounter> = Arc::new(SharedCounter::new());

    for i in 0..iteration_count {

        // cloned the reference to same data to distribute ownership to different threads
        let shared_counter = Arc::clone(&shared_counter);
        let counter_thread_name = format!("Counter-{}", i);

        // ideally should use the returned handler to join spawned threads for structured concurrency
        thread::Builder::new().name(counter_thread_name)
            .spawn(|| {
                count(shared_counter);
            });
    }

    // dummy joining as simplest way for waiting multiple concurrent threads
    thread::sleep(Duration::from_secs(1));

    // no dereferencing smart pointer with '*' so that reference is copied
    shared_counter
}

fn count(shared_counter: Arc<SharedCounter>) {
    let current_thread = thread::current();
    let thread_name = current_thread.name().unwrap_or("Unassigned");

    // mutex does not give write access directly, it requires the lock() to be called
    // because only mutex guard implements DerefMut hence can give away mutable reference
    let mut mutex_counter_guard = shared_counter.mutex_counter.lock().unwrap();
    *mutex_counter_guard += 1;
    println!("Incremented mutex counter: {} in thread {}", mutex_counter_guard, thread_name);

    let mut rwlock_counter_guard = shared_counter.rwlock_counter.write().unwrap();
    *rwlock_counter_guard += 1;
    println!("Incremented rwlock counter: {} in thread {}", rwlock_counter_guard, thread_name);
}
"##;