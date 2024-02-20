// Copyright

// go package
package main

import "fmt"

// doc
func main() {
	log(val)
	log(val1)
	line(value)

	t := &Class{}
	line2(t.a)
	line2(t.b)

	line(t)
	multiLine(t)
	lineBlock(t)
	block(t)

	// t
	a := &Class{}
	line(a)
	line(multiLine(block(t)))
}

/**
 * log
 */
func log(s string) {
	fmt.Print(s)
}

// const
const val = ""

const (
	// val1
	val1 = ""
)

// var
var value *Class

// Class First
type Class struct {
	// a
	a *Class2
	b *Class2 // b
}

// Class2 2
type Class2 struct{}

// line
func line(t *Class) *Class {
	return t
}

// line2
func line2(t *Class2) *Class2 {
	return t
}

/*** lineBlock */
func lineBlock(t *Class) *Class {
	return t
}

/*****
 ***** block
 *****/
func block(t *Class) *Class {
	return t
}

// other

//  1
//  2
func multiLine(t *Class) *Class {
	return t
}
