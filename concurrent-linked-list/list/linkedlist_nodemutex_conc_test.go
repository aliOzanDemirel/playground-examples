package list

import (
	"testing"
)

func TestListNodeMutex_concurrency_push(t *testing.T) {

	// 50 separate routines, each adding 3 nodes with total value (1+2+3)
	// final order of nodes does not matter, but:
	// - expect to find exactly 150 nodes in the list
	// - expect to find 300 for the sum of all nodes in the list
	threadCount := 50
	expectedCount, expectedSum := int64(threadCount*3), int64(threadCount*6)

	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_3pushes_6totalValue(t, list, threadCount)

	count := int64(0)
	sumOfAllNodes := uint64(0)
	currentNode := &list.head
	for currentNode != nil {
		count++
		sumOfAllNodes += currentNode.value
		currentNode = currentNode.next
	}
	if list.Size() != expectedCount || count != expectedCount {
		t.Errorf("wrong node count, expected: %d, actual size: %d, actual counted: %d", expectedCount, list.Size(), count)
	}
	if int64(sumOfAllNodes) != expectedSum {
		t.Errorf("wrong sum of node values, expected: %d, actual: %d", expectedSum, sumOfAllNodes)
	}
}

func TestListNodeMutex_concurrency_insertAfter(t *testing.T) {

	// 100 separate routines, each adding 4 nodes with total value 12 (1+2+3+6)
	// final order of nodes does not matter, but:
	// - expect to find exactly 400 nodes in the list
	// - expect to find 1200 for the sum of all nodes in the list
	threadCount := 100
	expectedCount, expectedSum := int64(threadCount*4), int64(threadCount*12)

	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_2pushes_2inserts_12totalValue(t, list, threadCount)

	count := int64(0)
	sumOfAllNodes := uint64(0)
	currentNode := &list.head
	for currentNode != nil {
		count++
		sumOfAllNodes += currentNode.value
		currentNode = currentNode.next
	}
	if list.Size() != expectedCount || count != expectedCount {
		t.Errorf("wrong node count, expected: %d, actual size: %d, actual counted: %d", expectedCount, list.Size(), count)
	}
	if int64(sumOfAllNodes) != expectedSum {
		t.Errorf("wrong sum of node values, expected: %d, actual: %d", expectedSum, sumOfAllNodes)
	}
}

func TestListNodeMutex_concurrency_pop_clearListFully(t *testing.T) {

	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_pop_clearListFully(t, list, 400, 8000)

	expectedRemainingNodeCount := int64(0)
	if list.Size() != expectedRemainingNodeCount {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedRemainingNodeCount, list.Size())
	}
}

func TestListNodeMutex_concurrency_pop_clearListPartially(t *testing.T) {

	expectedRemainingNodeCount := 7
	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_pop_clearListWithRemainingNodes(t, list, 400, 4000, expectedRemainingNodeCount)

	if list.Size() != int64(expectedRemainingNodeCount) {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedRemainingNodeCount, list.Size())
	}
}

func TestListNodeMutex_concurrency_pop_withPush(t *testing.T) {

	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_2pushes_2pops(t, list, 300)

	// whatever order of execution happens, end state of list should always be empty
	expectedCount := int64(0)
	if list.Size() != expectedCount {
		t.Errorf("wrong node count, expected: %d, actual: %d", expectedCount, list.Size())
	}
}

func TestListNodeMutex_concurrency_nondeterministic_mix(t *testing.T) {

	// end state cannot be determined and verified
	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_nondeterministic_mix(t, list, 300)
}

func TestListNodeMutex_concurrency_nondeterministic_random(t *testing.T) {

	// end state cannot be determined and verified
	list := new(SinglyLinkedListWithNodeMutex)
	concurrent_nondeterministic_random(t, list, 500)
}
