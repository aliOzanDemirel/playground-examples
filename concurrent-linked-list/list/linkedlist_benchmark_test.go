package list

import "testing"

func Benchmark_listNodeMutex(b *testing.B) {

	list := new(SinglyLinkedListWithNodeMutex)
	for i := 0; i < b.N; i++ {
		concurrent_nondeterministic_mix(b, list, 10000)
	}
	b.Logf("SinglyLinkedListWithNodeMutex benchmark run finished, list has total nodes: %d", list.count.Load())
}

func Benchmark_listGlobalMutex(b *testing.B) {

	list := new(SinglyLinkedListWithGlobalMutex)
	for i := 0; i < b.N; i++ {
		concurrent_nondeterministic_mix(b, list, 10000)
	}
	b.Logf("SinglyLinkedListWithGlobalMutex benchmark run finished, list has total nodes: %d", list.count)
}
