(ns fp-assignment2-robot-planning.data)

; all the corridor nodes in the building 
(def corridor [:mail :ts :c101 :c103 :c105 :c107 :c109 :c111 :c113 :c115
               :c117 :c118B :c118A :c119 :c121 :c123 :c125 :c127 :c129 
               :c131])


; all outer rooms in the building
(def outer-rooms [:r101 :r103 :r105 :r107 :r109 :r111 :r113 :r115
                  :r117 :r119 :r121 :r123 :r125 :r127 :r129 :r131])


; all inner of the building rooms
(def inner-rooms [:a1 :a2 :a3 :b1 :b2 :b3 :b4 :c1 :c2 :c3 :d1 :d2 :d3])


; all the special rooms in the building
(def special-rooms [:main-office :stairs :storage])


; all the rooms in one list
(def all-rooms (concat corridor 
                       outer-rooms 
                       inner-rooms
                       special-rooms))

; the parcles that each room has and its destination
(def parcel-register {:r101 []
                      :r103 []
                      :r105 []
                      :r107 []
                      :r109 []
                      :r111 []
                      :r113 []
                      :r115 []
                      :r117 []
                      :r119 []
                      :r121 []
                      :r123 []
                      :r125 []
                      :r127 []
                      :r129 []
                      :r131 []
                      :a1 []
                      :a2 []
                      :a3 []
                      :b1 []
                      :b2 []
                      :b3 []
                      :b4 []
                      :c1 []
                      :c2 []
                      :c3 []
                      :d1 []
                      :d2 []
                      :d3 []
                      :main-office []
                      :storage []
                      })




; all of the connectios between rooms
(def all-edges [[:r101 , :c101 , 2]
								[:r103 , :c103 , 2]
								[:r105 , :c105 , 2]
								[:r107 , :c107 , 2]
								[:r109 , :c109 , 2]
								[:r111 , :c111 , 2]
								[:r113 , :c113 , 2]
								[:r115 , :c115 , 2]
								[:r117 , :c117 , 2]
								[:r119 , :c119 , 2 ]
								[:r121 , :c121 , 2]
								[:r123 , :c123 , 2]
								[:r125 , :c125 , 2]
								[:r127 , :c127 , 2]
								[:r129 , :c129 , 2]
								[:r131 , :c131 , 2]
								[:a1 , :b1 , 1.5]
								[:a1 , :d3 , 2]
								[:a1 , :a3 , 1]
								[:a2 , :a3 , 1]
								[:a2 , :ts , 2]
								[:a3 , :c101 , 2]
								[:b1 , :b2 , 1]
								[:b1 , :c2 , 2]
								[:b1 , :b3 , 1]
								[:b2 , :c3 , 2]
								[:b2 , :b4 , 1]
								[:b3 , :b4 , 1]
								[:b3 , :c103 , 2]
								[:b4 , :c107 , 2]
								[:c1 , :c123 , 2]
								[:c2 , :c1 , 1]
								[:c2 , :c3 , 1]
								[:d1 , :c129 , 2]
								[:d1 , :d2 , 1]
								[:d2 , :c127 , 2]
								[:d2 , :d3 , 1]
								[:main-office , :mail , 3]
								[:stairs , :ts , 5]
								[:storage , :c118A , 3]
								[:mail , :ts , 1]
								[:mail , :c131 , 4]
								[:ts , :c101 , 1]
								[:c101 , :c103 , 1]
								[:c103 , :c105 , 1]
								[:c105 , :c107 , 1]
								[:c107 , :c109 , 1]
								[:c109 , :c111 , 1]
								[:c109 , :c113 , 1]
								[:c113 , :c115 , 1]
								[:c115 , :c117 , 1]
								[:c117 , :c118B , 0.5]
								[:c118B , :c118A , 0.5]
								[:c118A , :c119 , 1]
								[:c119 , :c121 , 1]
								[:c121 , :c123 , 1]
								[:c123 , :c125 , 1]
								[:c125 , :c127 , 1]
								[:c127 , :c129 , 1]
								[:c129 , :c131 , 1]])