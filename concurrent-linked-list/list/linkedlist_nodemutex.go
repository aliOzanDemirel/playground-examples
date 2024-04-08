package list

import (
	"sync/atomic"
)

// NOTE: some head related logic can be optimistically done without locking entrance of list

type SinglyLinkedListWithNodeMutex struct {
	count atomic.Int64 // how many nodes exist in the list, atomic because of potentially independent accesses from different nodes
	head  Node         // head is special node that always exists, holds first value node if list is initialized (count > 0)
}

func (l *SinglyLinkedListWithNodeMutex) Size() int64 {

	// atomically read and return the current value
	return l.count.Load()
}

func (l *SinglyLinkedListWithNodeMutex) Snapshot() []uint64 {

	// lock the start of list and iterate the rest to copy list to array
	l.head.guard.Lock()
	defer l.head.guard.Unlock()

	if l.Size() == 0 {
		return nil
	} else if l.Size() == 1 {
		return []uint64{l.head.value}
	}

	values := []uint64{l.head.value}
	current := l.head.next
	for current != nil {
		values = append(values, current.value)
		current = current.next
	}
	return values
}

func (l *SinglyLinkedListWithNodeMutex) Push(value uint64) {

	// lock the first node and handle head specific logic
	l.head.guard.Lock()

	if l.count.Load() == 0 {

		// this is first node, set head node to hold this value
		l.head.value = value
		l.count.Add(1)
		l.head.guard.Unlock()
		return
	} else if l.count.Load() == 1 {

		// this will be second node, attach to head node and do not attempt to iterate list
		l.head.next = &Node{value: value}
		l.count.Add(1)
		l.head.guard.Unlock()
		return
	}

	// execution coming here means that we have to iterate nodes to find tail
	// set head as previous (or left) node, we already hold the lock for this one
	previousNode := &l.head

	// start iteration by specifying second node as current (or right) node
	currentNode := l.head.next

	for {

		// attempt to acquire lock of current node that will be treated as our right node
		currentNode.guard.Lock()

		// left node's lock is released only after we could move on to next node
		previousNode.guard.Unlock()

		// check if current (or right, but really just current at this point) node is last node
		isTail := currentNode.next == nil
		if isTail {
			// if this is last node, create new node and link it to this current node
			newNode := Node{value: value}
			currentNode.next = &newNode
			l.count.Add(1)

			// then unlock current (once last node but not anymore) node and exit loop
			currentNode.guard.Unlock()
			break
		}

		// if we did not find tail, move to next node by holding on to current node as previous (or left) node
		previousNode = currentNode

		// make the move to right, this is okay since we still hold the lock and nothing could modify our next node
		currentNode = currentNode.next
	}
}

func (l *SinglyLinkedListWithNodeMutex) InsertAfter(value, afterValue uint64) bool {

	// lock the first node and handle head specific logic
	l.head.guard.Lock()

	if l.count.Load() == 0 {
		// no-op since list is empty, no node to search for 'afterValue'
		l.head.guard.Unlock()

		// could not find 'afterValue' and could not insert 'value'
		return false
	} else {

		// if list has any node, check if head is the node we are searching for (having value 'afterValue')
		if l.head.value == afterValue {

			// if head is same as 'afterValue', add new node right next to head and return
			newNode := Node{
				value: value,
				next:  l.head.next,
			}
			l.head.next = &newNode
			l.count.Add(1)

			l.head.guard.Unlock()
			return true
		}

		// list has only one node and it did not match 'afterValue', unlock head and return
		if l.count.Load() == 1 {
			l.head.guard.Unlock()

			// could not find 'afterValue' and could not insert 'value'
			return false
		}
	}

	// code coming here means that we have to iterate list to find 'afterValue'
	previousNode := &l.head
	currentNode := l.head.next

	for {

		// iterate just like in 'Push' method, by not releasing lock of left node until we could move to right
		// moving to right means actually acquiring lock of that node and excluding any other thread from here
		currentNode.guard.Lock()
		previousNode.guard.Unlock()

		// check if this node has the value we are searching for
		isTargetNode := currentNode.value == afterValue
		if isTargetNode {

			// if target node exists and is found, add new node right after it and link new node to target's next
			newNode := Node{
				value: value,
				next:  currentNode.next,
			}
			currentNode.next = &newNode
			l.count.Add(1)

			// inserted new node after the first node with 'afterValue' is found, now return
			// does not care if there are other nodes with 'afterValue' later in the list
			currentNode.guard.Unlock()
			return true
		}

		// if execution comes here, check if current node is tail node
		// if we are at tail, then exit the search, we could not find 'afterValue'
		isTail := currentNode.next == nil
		if isTail {
			currentNode.guard.Unlock()
			return false
		}

		// if this node does not have searched value 'afterValue', move to next node
		previousNode = currentNode
		currentNode = currentNode.next
	}
}

func (l *SinglyLinkedListWithNodeMutex) Pop() bool {

	// lock the first node and handle head specific logic
	l.head.guard.Lock()

	if l.count.Load() == 0 {
		// no-op since list is empty, no node to remove
		l.head.guard.Unlock()

		// did not pop anything, signal that with false
		return false
	} else if l.count.Load() == 1 {

		// list has only one node, remove head by zero-ing struct fields
		l.head.value = 0
		l.head.next = nil
		l.count.Add(-1)

		l.head.guard.Unlock()
		return true
	}

	// execution coming here means we have to iterate rest of the list
	currentNode := &l.head
	for {

		// attempt to hold lock of next node as well as the current node we are at
		nextNode := currentNode.next
		nextNode.guard.Lock()

		// after successfully controlling next node, check if that is tail that should be cut from list
		isNextNodeTail := nextNode.next == nil
		if isNextNodeTail {

			// if next node is tail, cut the link of current node
			currentNode.next = nil
			l.count.Add(-1)

			// release locks in order they are taken, and exit the loop
			nextNode.guard.Unlock()
			currentNode.guard.Unlock()
			return true
		}

		// since next node is not tail, we should move one node to the right, leaving behind our current (or left) node
		// since we are leaving this node, we can now release its lock
		currentNode.guard.Unlock()

		// our next (or right) node that is checked if it is tail, now becomes current (or left)
		// its lock is still kept until new next node is locked, so that no other pop can claim our new current node
		currentNode = nextNode
	}
}
