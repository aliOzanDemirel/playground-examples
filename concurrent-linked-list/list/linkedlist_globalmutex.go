package list

import (
	"sync"
)

// SinglyLinkedList represents thread safe singly linked list. this list stores integer values for simplicity, instead of an object/struct.
// - Push(Object o) -> add node to the end of list
// - Pop() -> remove last node
// --- return false if list is already empty
// - InsertAfter(Object o, Object after) -> insert node next to after node
// --- assumed that insertion won't happen if 'after' is not found, returning false
// --- insertion will happen after first value that is equal to 'after' is found, ignoring other values after that node
// - Size() -> returns total number of nodes, added for inspection
// - Snapshot() -> copy and return all available values
type SinglyLinkedList interface {
	Push(value uint64)
	Pop() bool
	InsertAfter(value, afterValue uint64) bool
	Size() int64
	Snapshot() []uint64
}

// SinglyLinkedListWithGlobalMutex is synchronized by a global lock
type SinglyLinkedListWithGlobalMutex struct {
	count       int64 // how many nodes exist in the list
	head        *Node
	guardGlobal sync.Mutex
}

// Node represents item stored in the list
type Node struct {
	value uint64
	next  *Node
	guard sync.Mutex
}

func (l *SinglyLinkedListWithGlobalMutex) Size() int64 {

	l.guardGlobal.Lock()
	defer l.guardGlobal.Unlock()

	return l.count
}

func (l *SinglyLinkedListWithGlobalMutex) Snapshot() []uint64 {

	l.guardGlobal.Lock()
	defer l.guardGlobal.Unlock()

	var values []uint64
	current := l.head
	for current != nil {
		values = append(values, current.value)
		current = current.next
	}
	return values
}

func (l *SinglyLinkedListWithGlobalMutex) Push(value uint64) {

	l.guardGlobal.Lock()
	defer l.guardGlobal.Unlock()

	if l.head == nil {

		firstNode := Node{value: value}
		l.head = &firstNode
	} else {

		_, tail := l.findLastTwoNodes()
		newNode := Node{value: value}
		tail.next = &newNode
	}

	l.count++
}

func (l *SinglyLinkedListWithGlobalMutex) Pop() bool {

	l.guardGlobal.Lock()
	defer l.guardGlobal.Unlock()

	if l.head == nil {
		// no-op, nothing to pop
		return false
	} else if l.head.next == nil {
		l.head = nil
		l.count = 0
		return true
	}

	lastNodeBeforeTail, _ := l.findLastTwoNodes()
	lastNodeBeforeTail.next = nil
	l.count--
	return true
}

func (l *SinglyLinkedListWithGlobalMutex) InsertAfter(value, afterValue uint64) bool {

	l.guardGlobal.Lock()
	defer l.guardGlobal.Unlock()

	if l.head == nil {
		// no-op
		return false
	}

	afterValueNode := l.head
	for afterValueNode != nil {

		if afterValueNode.value == afterValue {
			break
		}
		afterValueNode = afterValueNode.next
	}

	// if not nil, found the equivalent node
	if afterValueNode != nil {
		newNode := &Node{
			value: value,
			next:  afterValueNode.next,
		}
		afterValueNode.next = newNode
		l.count++
		return true
	}
	return false
}

func (l *SinglyLinkedListWithGlobalMutex) findLastTwoNodes() (*Node, *Node) {

	if l.head == nil {
		return nil, nil
	}

	var lastNodeBeforeTail *Node
	tail := l.head
	for tail.next != nil {
		lastNodeBeforeTail = tail
		tail = tail.next
	}
	return lastNodeBeforeTail, tail
}
