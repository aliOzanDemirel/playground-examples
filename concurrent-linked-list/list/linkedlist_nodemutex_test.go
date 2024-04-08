package list

import (
	"testing"
)

func TestListNodeMutex_snapshot(t *testing.T) {

	t.Run("empty list", func(t *testing.T) {

		list := &SinglyLinkedListWithNodeMutex{head: Node{}}
		values := list.Snapshot()
		if len(values) != 0 {
			t.Errorf("wrong value count, expected: 0, actual: %d", len(values))
		}
	})

	t.Run("single node list", func(t *testing.T) {

		list := &SinglyLinkedListWithNodeMutex{
			head: Node{value: 999},
		}
		list.count.Add(1)

		values := list.Snapshot()
		if len(values) != 1 {
			t.Errorf("wrong value count, expected: 1, actual: %d", len(values))
		}
		if values[0] != 999 {
			t.Errorf("wrong first value, expected: 999, actual: %d", values[0])
		}
	})

	t.Run("many nodes list", func(t *testing.T) {

		list := &SinglyLinkedListWithNodeMutex{
			head: Node{
				value: 111,
				next: &Node{
					value: 222,
					next: &Node{
						value: 333,
					},
				},
			},
		}
		list.count.Add(3)

		values := list.Snapshot()
		if len(values) != 3 {
			t.Errorf("wrong value count, expected: 3, actual: %d", len(values))
		}
		if values[0] != 111 {
			t.Errorf("wrong first value, expected: 111, actual: %d", values[0])
		}
		if values[1] != 222 {
			t.Errorf("wrong second value, expected: 222, actual: %d", values[1])
		}
		if values[2] != 333 {
			t.Errorf("wrong second value, expected: 333, actual: %d", values[2])
		}
	})
}

func TestListNodeMutex_pop(t *testing.T) {

	t.Run("empty list", func(t *testing.T) {

		list := &SinglyLinkedListWithNodeMutex{head: Node{}}
		result := list.Pop()
		if result != false {
			t.Errorf("expected 'false' from empty list pop")
		}
		if list.Size() != 0 {
			t.Errorf("wrong node count, expected: 0, actual: %d", list.Size())
		}
	})

	t.Run("single node list", func(t *testing.T) {

		list := &SinglyLinkedListWithNodeMutex{
			head: Node{
				next: &Node{},
			},
		}
		list.count.Add(1)

		result := list.Pop()
		if result != true {
			t.Errorf("expected 'true' from empty list pop")
		}
		if list.Size() != 0 {
			t.Errorf("wrong node count, expected: 0, actual: %d", list.Size())
		}
	})

	t.Run("many nodes list", func(t *testing.T) {

		list := &SinglyLinkedListWithNodeMutex{
			head: Node{
				next: &Node{
					next: &Node{},
				},
			},
		}
		list.count.Add(2)

		result := list.Pop()
		if result != true {
			t.Errorf("expected 'true' from empty list pop")
		}
		if list.Size() != 1 {
			t.Errorf("wrong node count, expected: 1, actual: %d", list.Size())
		}
	})
}

// Pop empty -
// Push 1 - 1
// Pop -
// Push 2 - 2
// Push 3 - 2, 3
// Pop - 2
func TestListNodeMutex_correctness_scenario1(t *testing.T) {

	list := new(SinglyLinkedListWithNodeMutex)

	list.Pop()
	if list.Size() != 0 {
		t.Errorf("wrong node count, expected: 0, actual: %d", list.Size())
	}

	list.Push(1)
	if list.Size() != 1 {
		t.Errorf("wrong node count, expected: 1, actual: %d", list.Size())
	}
	if list.head.value != 1 {
		t.Errorf("wrong head node value, expected: 1, actual: %d", list.head.value)
	}
	if list.head.next != nil {
		t.Errorf("wrong link in head node, expected to find nil, actual: %d", list.head.next.value)
	}

	list.Pop()
	if list.Size() != 0 {
		t.Errorf("wrong node count, expected: 0, actual: %d", list.Size())
	}
	if list.head.value != 0 || list.head.next != nil {
		t.Errorf("wrong head, expected to find zero value, actual: %d", list.head.value)
	}

	list.Push(2)
	if list.Size() != 1 {
		t.Errorf("wrong node count, expected: 1, actual: %d", list.Size())
	}
	if list.head.value != 2 {
		t.Errorf("wrong head node value, expected: 2, actual: %d", list.head.value)
	}
	if list.head.next != nil {
		t.Errorf("wrong link in head node, expected to find nil, actual: %d", list.head.next.value)
	}

	list.Push(3)
	if list.Size() != 2 {
		t.Errorf("wrong node count, expected: 2, actual: %d", list.Size())
	}
	first, second := &list.head, list.head.next
	if first.value != 2 {
		t.Errorf("wrong value in first node, expected: 2, actual: %d", first.value)
	}
	if second.value != 3 {
		t.Errorf("wrong value in second node, expected: 3, actual: %d", second.value)
	}
	if second.next != nil {
		t.Errorf("wrong link in tail node, expected to find nil, actual: %d", second.next.value)
	}

	list.Pop()
	if list.Size() != 1 {
		t.Errorf("wrong node count, expected: 1, actual: %d", list.Size())
	}
	if list.head.value != 2 {
		t.Errorf("wrong head node value, expected: 2, actual: %d", list.head.value)
	}
	if list.head.next != nil {
		t.Errorf("wrong link in head node, expected to find nil, actual: %d", list.head.next.value)
	}
}

// insert 1 after 5 -
// Push 1 - 1
// insert 2 after 5 - 1
// insert 2 after 1 - 1, 2
// insert 3 after 1 - 1, 3, 2
// Pop - 1, 3
// insert 3 after 3 - 1, 3, 3
// insert 77 after 3 - 1, 3, 77, 3
func TestListNodeMutex_correctness_scenario2(t *testing.T) {

	list := new(SinglyLinkedListWithNodeMutex)

	list.InsertAfter(1, 5)
	if list.Size() != 0 {
		t.Errorf("wrong node count, expected: 0, actual: %d", list.Size())
	}
	if list.head.value != 0 || list.head.next != nil {
		t.Errorf("wrong head, expected to find zero value, actual: %d", list.head.value)
	}

	list.Push(1)
	if list.Size() != 1 {
		t.Errorf("wrong node count, expected: 1, actual: %d", list.Size())
	}
	if list.head.value != 1 {
		t.Errorf("wrong head node value, expected: 1, actual: %d", list.head.value)
	}
	if list.head.next != nil {
		t.Errorf("wrong link in head node, expected to find nil, actual: %d", list.head.next.value)
	}

	list.InsertAfter(2, 5)
	if list.Size() != 1 {
		t.Errorf("wrong node count, expected: 1, actual: %d", list.Size())
	}
	if list.head.value != 1 {
		t.Errorf("wrong head node value, expected: 1, actual: %d", list.head.value)
	}

	list.InsertAfter(2, 1)
	if list.Size() != 2 {
		t.Errorf("wrong node count, expected: 2, actual: %d", list.Size())
	}
	first, second := &list.head, list.head.next
	if first.value != 1 {
		t.Errorf("wrong value in first node, expected: 1, actual: %d", first.value)
	}
	if second.value != 2 {
		t.Errorf("wrong value in second node, expected: 2, actual: %d", second.value)
	}
	if second.next != nil {
		t.Errorf("wrong link in tail node, expected to find nil, actual: %d", second.next.value)
	}

	list.InsertAfter(3, 1)
	if list.Size() != 3 {
		t.Errorf("wrong node count, expected: 3, actual: %d", list.Size())
	}
	first, second, third := &list.head, list.head.next, list.head.next.next
	if first.value != 1 {
		t.Errorf("wrong value in first node, expected: 1, actual: %d", first.value)
	}
	if second.value != 3 {
		t.Errorf("wrong value in second node, expected: 3, actual: %d", second.value)
	}
	if third.value != 2 {
		t.Errorf("wrong value in third node, expected: 2, actual: %d", third.value)
	}
	if third.next != nil {
		t.Errorf("wrong link in tail node, expected to find nil, actual: %d", third.next.value)
	}

	list.Pop()
	if list.Size() != 2 {
		t.Errorf("wrong node count, expected: 2, actual: %d", list.Size())
	}

	list.InsertAfter(3, 3)
	if list.Size() != 3 {
		t.Errorf("wrong node count, expected: 3, actual: %d", list.Size())
	}
	first, second, third = &list.head, list.head.next, list.head.next.next
	if first.value != 1 {
		t.Errorf("wrong value in first node, expected: 1, actual: %d", first.value)
	}
	if second.value != 3 {
		t.Errorf("wrong value in second node, expected: 3, actual: %d", second.value)
	}
	if third.value != 3 {
		t.Errorf("wrong value in third node, expected: 3, actual: %d", third.value)
	}
	if third.next != nil {
		t.Errorf("wrong link in tail node, expected to find nil, actual: %d", third.next.value)
	}

	list.InsertAfter(77, 3)
	if list.Size() != 4 {
		t.Errorf("wrong node count, expected: 4, actual: %d", list.Size())
	}
	first, second, third, fourth := &list.head, list.head.next, list.head.next.next, list.head.next.next.next
	if first.value != 1 {
		t.Errorf("wrong value in first node, expected: 1, actual: %d", first.value)
	}
	if second.value != 3 {
		t.Errorf("wrong value in second node, expected: 3, actual: %d", second.value)
	}
	if third.value != 77 {
		t.Errorf("wrong value in third node, expected: 77, actual: %d", third.value)
	}
	if fourth.value != 3 {
		t.Errorf("wrong value in third node, expected: 3, actual: %d", fourth.value)
	}
	if fourth.next != nil {
		t.Errorf("wrong link in tail node, expected to find nil, actual: %d", fourth.next.value)
	}
}
