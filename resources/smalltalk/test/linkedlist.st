class Link : Object [
   | _value link |
   class withValue: v [
       "Call Object>>new, then set _value to arg v. same as '^self new' here
        since we don't define new in Link."
       ^super new value: v.
   ]

   nextLink [ ^link ]
   nextLink: aLink [ link := aLink ]
   value: v [ _value := v. ]
   value [^_value]
   asString [^_value asString]
]

class LinkedList : Collection [
   | head tail |
   first [^head]
   last [^tail]
   isEmpty [ ^head == nil ]
   add: v [ self addLast: v ]
   addFirst: v [
       | x |
       x := Link withValue: v.
       x nextLink: head.
       head := x
   ]
   addLast: v [
       self isEmpty
           ifTrue:  [ head := Link withValue: v. tail := head ]
           ifFalse: [ tail nextLink: (Link withValue: v). tail := tail nextLink ].
   ]
   removeAll [ head := nil. tail := nil ]
   removeFirst [
       head == nil ifFalse: [
           head := head nextLink.
           head==nil ifTrue: [tail:=nil].
       ]
   ]
   removeAllSuchThat: blk [
       self
           do: [:v | (blk value: v) ifTrue: [self remove: v]]
   ]
   remove: x [
       | prev p |
       prev := nil.
       p := head.
       [p~~nil] whileTrue: [
           p value = x ifTrue: [
               prev==nil
                   ifTrue:  [ self removeFirst ]
                   ifFalse: [
                       prev nextLink: p nextLink.
                       p==tail ifTrue: [tail := prev]
                   ].
           ].
           prev := p.
           p := p nextLink.
       ]
   ]
   do: blk [
       |p|
       p:=head.
       [p~~nil] whileTrue: [ blk value: (p value). p := p nextLink ]
   ]
]

