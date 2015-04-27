class Object [
    "Class methods come first then instance methods

    Note that we allow class method inheritance (unlike Java) so 'super new'
    refers to the #new method of the superclass.

    All objects are initialized automatically by #new, unlike ST-80, but like
    Squeak.
    "
    class error: msg <primitive:#Object_Class_ERROR>
    class basicNew <primitive:#Object_Class_BASICNEW>
    class new [
       "Return a new, initialized object. Without ^, #new returns self,
        which is a metaclass object. Not what we want. Don't override #new
        but if you do, use ^ as in '^super new'. Don't call initialize
        in subclasses otherwise it calls initialize twice,
        once in subclass and once here in Object>>new.
        You must, however, have initialize call
        super initialize to init all fields up inheritance chain."
       ^self basicNew initialize
    ]

    class new: x [
       "Create new object with an arg. Requires that you implement initialize: x
        like: initialize [ super initialize. your-init-code. ^self ]"
       ^self new initialize: x
    ]

    initialize [^self]
    initialize: x [^self]
    print <primitive:#Object_PRINT>
    className <primitive:#Object_CLASSNAME>
    asString <primitive:#Object_ASSTRING>
    == anObject <primitive:#Object_SAME>
    ~~ anObject [ ^(self == anObject) not ]
    = anObject [
       "receiver and argument are equal (like Java equals()). default to =="
       ^self == anObject
    ]
    ~= anObject [
       "receiver and argument aren't same value"
       ^(self = anObject) not
    ]
    -> anObject [
        ^Association of: self to: anObject
    ]
    hash <primitive:#Object_HASH>
]

class UndefinedObject : Object [
    "nil's class backed by Java class STNil"
    asString [^'nil']
]

class BlockDescriptor : Object [
   "An object that represents a Smalltalk code block backed by Java class BlockDescriptor"
   value                   <primitive:#BlockDescriptor_VALUE>
   value: a                <primitive:#BlockDescriptor_VALUE_1_ARG>
   value: a value: b       <primitive:#BlockDescriptor_VALUE_2_ARGS>
   whileTrue: blk [
       (self value) ifTrue: [blk value. self whileTrue: blk]
   ]
]

class Character : Object [
   "An object that represents a Smalltalk character backed by Java class STCharacter"
   class new: i <primitive:#Character_Class_NEW>
   asInteger <primitive:#Character_ASINTEGER>
   + c [ ^Character new: self asInteger + c ]
   , c [ ^self asString, c asString ]
   asString [ ^String new: self ]
   hash [^self asInteger]
]

class String : Object [
   "An object that represents a Smalltalk string backed by Java class STString"
   class new [ ^self new: 10 ]
   class new: c <primitive:#String_Class_NEW>

   , other <primitive:#String_CAT>
   asArray <primitive:#String_ASARRAY>
   at: i [ |chars| chars := self asArray. ^chars at: i ]
   do: blk [ self asArray do: blk ]
   size [ ^self asArray size ]
   hash [
       |h|
       h := 0.
       self do: [:c | h := h + c asInteger].
       ^h
   ]
   asString [ ^self ]
   = s <primitive:#String_EQ>
]

class Boolean : Object [
   "An object that represents a Smalltalk Boolean backed by Java class STBoolean"
   not <primitive:#Boolean_NOT>
   ifTrue: blk <primitive:#Boolean_IFTRUE>
   ifTrue: blk ifFalse: else <primitive:#Boolean_IFTRUE_IFFALSE>
   ifFalse: blk [ ^self not ifTrue: blk ]
   and: b [
       self ifTrue: [ b ifTrue: [^true] ].
       ^false.
   ]
   or: b [
       self ifTrue: [ ^true ].
       b ifTrue: [ ^true ].
       ^false.
   ]
]

class Number : Object [
]

class Integer : Number [
   "this object has no fields visible from smalltalk code and is backed by Java class STInteger"
   + y <primitive:#Integer_ADD>
   - y <primitive:#Integer_SUB>
   * y <primitive:#Integer_MULT>
   / y <primitive:#Integer_DIV>
   < y <primitive:#Integer_LT>
   > y <primitive:#Integer_GT>
   <= y <primitive:#Integer_LE>
   >= y <primitive:#Integer_GE>
   = y <primitive:#Integer_EQ>
   to: n do: blk [
       self <= n ifTrue: [blk value: self. self+1 to: n do: blk]
   ]
   hash [ ^self ]
   mod: n <primitive:#Integer_MOD>
]

class Float : Number [
   "this object has no fields visible from smalltalk code, backed by Java class STFloat"
   + y <primitive:#Float_ADD>
   - y <primitive:#Float_SUB>
   * y <primitive:#Float_MULT>
   / y <primitive:#Float_DIV>
   < y <primitive:#Float_LT>
   > y <primitive:#Float_GT>
   <= y <primitive:#Float_LE>
   >= y <primitive:#Float_GE>
   = y <primitive:#Float_EQ>
   to: n do: blk [
       self <= n ifTrue: [blk value: self. self+1 to: n do: blk]
   ]
   asInteger <primitive:#Float_ASINTEGER>
   hash [ ^self ]
]

class Association : Object [
    "A key-value association. Create with anObject -> anObject."
    | _key _value |
    class of: x to: y [ ^self basicNew initializeWith: x and: y ]
    initializeWith: key and: value [ _key := key. _value := value]
    key: k [ _key := k ]
    value: v [ _value := v ]
    key [^_key]
    value [^_value]
    asString [ ^_key asString, '->', _value asString ]
]

class Collection : Object [
    size [
        | n |
        n := 0.
        self do: [:each | n := n + 1].
        ^n
    ]
    map: f [
        | dup i |
        dup := Array new: self size.
        i := 1.
        self do: [:v | dup at: i put: (f value: v). i := i + 1].
        ^dup
    ]
    filter: cond [
        |data|
        data := LinkedList new.
        self do: [:v | (cond value: v) ifTrue: [data add: v]].
        ^data
    ]
    asString [
        | s first |
        self size = 0 ifTrue: [^self className, '()'].
        s := self className, '('.
        first := true.
        self do: [:v |
           first ifTrue:  [first:=false. s := s, v asString.]
                 ifFalse: [s := s, '. ', v asString.]
           ].
        s := s, ')'.
        ^s
    ]
]

class Array : Collection [
   "An object that represents a Smalltalk array of objects backed by Java class STArray"
   class new [ ^self new: 10 ]
   class new: size <primitive:#Array_Class_NEW>

   size <primitive:#Array_SIZE>
   at: i <primitive:#Array_AT>
   at: i put: v <primitive:#Array_AT_PUT>
   do: blk [
       1 to: self size do: [:i | blk value: (self at: i)].
   ]
]

class Stream : Object [
]

class WriteStream : Stream [
]

class ReadStream : Stream [
]

class TranscriptStream : WriteStream [
    show: o <primitive:#TranscriptStream_SHOW>
]
