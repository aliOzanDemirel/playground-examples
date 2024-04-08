package list

import (
	"math/rand"
	"sync"
	"testing"
	"time"
)

// assume tests with concurrent goroutines should finish before this timeout, or a potential deadlock exists
const timeout = 5 * time.Second

func concurrent_3pushes_6totalValue(t testing.TB, list SinglyLinkedList, threadCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	go func() {
		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			go func() {
				defer wait.Done()

				list.Push(1)
				list.Push(2)
				list.Push(3)
			}()
		}
		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

func concurrent_2pushes_2inserts_12totalValue(t testing.TB, list SinglyLinkedList, threadCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	go func() {
		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			go func() {
				defer wait.Done()

				list.Push(1)
				list.InsertAfter(888, 999)
				list.InsertAfter(2, 1)
				list.InsertAfter(3, 2)
				list.Push(6)
				list.InsertAfter(777, 888)
			}()
		}
		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

func concurrent_2pushes_2pops(t testing.TB, list SinglyLinkedList, threadCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	go func() {
		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			go func() {
				defer wait.Done()

				list.Push(888)
				list.Pop()
				list.Push(999)
				list.Pop()
			}()
		}
		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

func concurrent_pop_clearListWithRemainingNodes(t testing.TB, list SinglyLinkedList, threadCount, totalNodeCount, expectedRemainingNodeCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	for i := 0; i < totalNodeCount; i++ {
		list.Push(uint64(i))
	}

	popCountPerThread := totalNodeCount / threadCount
	go func() {

		// this thread will add new nodes every 10th millis
		wait.Add(1)
		go func() {
			defer wait.Done()

			for i := 0; i < expectedRemainingNodeCount; i++ {
				time.Sleep(10 * time.Millisecond)
				list.Push(999_999)
			}
		}()

		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			go func() {
				defer wait.Done()

				for i := 0; i < popCountPerThread; i++ {
					list.Pop()
				}
			}()
		}

		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

// prepare a list with a lot of nodes, then run concurrent Pop operations on it, make sure that all items are gone
func concurrent_pop_clearListFully(t testing.TB, list SinglyLinkedList, threadCount, totalNodeCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	for i := 0; i < totalNodeCount; i++ {
		list.Push(uint64(i))
	}

	popCountPerThread := totalNodeCount / threadCount
	go func() {
		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			go func() {
				defer wait.Done()

				for i := 0; i < popCountPerThread; i++ {
					list.Pop()
				}
			}()
		}

		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

func concurrent_nondeterministic_mix(t testing.TB, list SinglyLinkedList, threadCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	go func() {
		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			go func() {
				defer wait.Done()

				list.Pop()
				list.Push(10)
				list.Pop()
				list.InsertAfter(20, 10)
				list.InsertAfter(30, 20)
				list.Push(40)
				list.Pop()
				list.Push(50)
				list.Pop()
				list.Pop()
				list.Pop()
				list.Push(60)
				list.InsertAfter(50, 10)
				list.Pop()
			}()
		}
		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

func concurrent_nondeterministic_random(t testing.TB, list SinglyLinkedList, threadCount int) {

	wait := &sync.WaitGroup{}
	signalWaitDone := make(chan struct{})

	go func() {
		constantlyPushingFunc := func(times int) {
			defer wait.Done()
			for i := 0; i < times; i++ {
				list.Push(uint64(i))
			}
		}
		constantlyInsertingFunc := func(times int) {
			defer wait.Done()
			for i := 0; i < times; i++ {
				list.InsertAfter(uint64(i), 500)
			}
		}
		constantlyPoppingFunc := func(times int) {
			defer wait.Done()
			for i := 0; i < times; i++ {
				list.Pop()
			}
		}

		for i := 0; i < threadCount; i++ {
			wait.Add(1)
			randomNum := rand.Intn(99)
			decideWhichFunc := randomNum % 3
			if decideWhichFunc == 0 {
				go constantlyPushingFunc(randomNum)
			} else if decideWhichFunc == 1 {
				go constantlyInsertingFunc(randomNum)
			} else if decideWhichFunc == 2 {
				go constantlyPoppingFunc(randomNum)
			}
		}

		wait.Wait()
		close(signalWaitDone)
	}()

	select {
	case <-signalWaitDone:
		t.Logf("concurrent operations finished before timeout")
	case <-time.After(timeout):
		t.Fatalf("test timeout after %v", timeout)
	}
}

func TestListGlobalMutex_concurrency_push(t *testing.T) {

	// 50 separate routines, each adding 3 nodes with total value (1+2+3)
	// final order of nodes does not matter, but:
	// - expect to find exactly 150 nodes in the list
	// - expect to find 300 for the sum of all nodes in the list
	threadCount := 50
	expectedCount, expectedSum := int64(threadCount*3), int64(threadCount*6)

	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_3pushes_6totalValue(t, list, threadCount)

	if list.count != expectedCount {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedCount, list.count)
	}

	sumOfAllNodes := uint64(0)
	currentNode := list.head
	for currentNode != nil {
		sumOfAllNodes += currentNode.value
		currentNode = currentNode.next
	}

	if int64(sumOfAllNodes) != expectedSum {
		t.Errorf("wrong sum of node values, expected: %d, actual: %d", expectedSum, sumOfAllNodes)
	}
}

func TestListGlobalMutex_concurrency_insertAfter(t *testing.T) {

	// 100 separate routines, each adding 4 nodes with total value 12 (1+2+3+6)
	// final order of nodes does not matter, but:
	// - expect to find exactly 400 nodes in the list
	// - expect to find 300 for the sum of all nodes in the list
	threadCount := 100
	expectedCount, expectedSum := int64(threadCount*4), int64(threadCount*12)

	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_2pushes_2inserts_12totalValue(t, list, threadCount)

	if list.count != expectedCount {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedCount, list.count)
	}

	sumOfAllNodes := uint64(0)
	currentNode := list.head
	for currentNode != nil {
		sumOfAllNodes += currentNode.value
		currentNode = currentNode.next
	}

	if int64(sumOfAllNodes) != expectedSum {
		t.Errorf("wrong sum of node values, expected: %d, actual: %d", expectedSum, sumOfAllNodes)
	}
}

func TestListGlobalMutex_concurrency_pop_clearListFully(t *testing.T) {

	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_pop_clearListFully(t, list, 400, 8000)

	expectedRemainingNodeCount := int64(0)
	if list.count != expectedRemainingNodeCount {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedRemainingNodeCount, list.count)
	}
}

func TestListGlobalMutex_concurrency_pop_clearListPartially(t *testing.T) {

	expectedRemainingNodeCount := 7
	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_pop_clearListWithRemainingNodes(t, list, 400, 4000, expectedRemainingNodeCount)

	if list.count != int64(expectedRemainingNodeCount) {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedRemainingNodeCount, list.count)
	}
}

func TestListGlobalMutex_concurrency_pop_withPush(t *testing.T) {

	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_2pushes_2pops(t, list, 300)

	// whatever order of execution happens, end state of list should always be empty
	expectedCount := int64(0)
	if list.count != expectedCount {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedCount, list.count)
	}
}

func TestListGlobalMutex_concurrency_nondeterministic_mix(t *testing.T) {

	// end state cannot be determined and verified
	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_nondeterministic_mix(t, list, 300)
}

func TestListGlobalMutex_concurrency_nondeterministic_random(t *testing.T) {

	// end state cannot be determined and verified
	list := new(SinglyLinkedListWithGlobalMutex)
	concurrent_nondeterministic_random(t, list, 500)
}
