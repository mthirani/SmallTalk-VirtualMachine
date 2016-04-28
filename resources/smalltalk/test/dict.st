class Dictionary : Collection [
    | buckets n nbuckets |

    class newFrom: list [
        |d|
        d := Dictionary new.
        list do: [:a | d at: a key put: a value].
        ^d
    ]

    initialize [
        n := 0.
        nbuckets := 7.
        buckets := Array new: nbuckets.
        1 to: nbuckets do: [:i | buckets at: i put: LinkedList new].
    ]

    at: key [
        |b|
        b := buckets at: ((key hash) mod: nbuckets) + 1.
        b do: [:assoc | assoc key = key ifTrue: [^assoc value] ].
        ^nil
    ]

    at: key put: value [
        |b a|
        b := buckets at: ((key hash) mod: nbuckets) + 1.
        b do: [:assoc |
           assoc key = key ifTrue: [^assoc value: value].
           ^self
        ].
        a := Association of: key to: value.
        b add: a.
    ]

    do: blk [
        buckets do: [:bucket | bucket do: [:assoc | blk value: assoc]].
    ]

    keys [
        ^self map: [:assoc | assoc key]
    ]

    values [
        ^self map: [:assoc | assoc value]
    ]

    size [
        "Answer how many elements the receiver contains."
        | c |
        c := 0.
        self do: [:each | c := c + 1].
        ^c
    ]
    removeAll [ self initialize ]
]