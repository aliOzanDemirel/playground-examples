package business.port;

// marks the APIs that are contracts of services, implementations of these are to be provided by the outer layer.
// in hexagonal architecture terminology, this is basically port for external adapters of the outer layer that
// has no business logic concerns, implementation of technical wiring code.
public @interface ExternalAdapterApi {
}
