(ns fp-assignment2-robot-planning.core)
(require '[fp-assignment2-robot-planning.data :as data])



;-------------------------------------------------------------------------------
(defn alist-add-vertex [adj-list vertex-name]
  "Add vertex 'vertex-name' with no edges to adjacency list 'adj-list' "
  (conj adj-list  [vertex-name {}]))



;-------------------------------------------------------------------------------
(defn alist-add-edge [adj-list vert-a vert-b weight]
  "Add a weighted edge between two verticies in the adjacency list"
  
  (if (and                                           ; check if the list has both veritcies to begin with
        (contains? adj-list vert-a)
        (contains? adj-list vert-b)) 
    
    (assoc-in (assoc-in adj-list                     ; TRUE - associate verticies with one another, give each the same weight
                        [vert-a vert-b] 
                        weight)
              [vert-b vert-a]
              weight)
    adj-list))                                       ; FALSE - return the list if the verticies are not present



;-------------------------------------------------------------------------------
(defn alist-add-all-edges [adj-list edges]
  "Add all of the edges in the given list 'edges' to 'adj-list' "
  (loop [graph adj-list
         edge-list edges
         curr-idx 0]
    
    (let [[vert-a vert-b weight] (get edge-list curr-idx)]       ; destructure each item in the list - into vertex a/b and weight
      (if vert-a                                                 ; check to see if we've reached end, if so arg will be 'nil'
        
       (recur (alist-add-edge graph vert-a vert-b weight)        ; 1st arg: new graph with edge added
              edge-list                                          ; 2nd arg: the same edge list
              (inc curr-idx))                                    ; 3rd arg: increment index to move to next item in edge-list
      
       graph))))                                                 ; return the graph as we've added all items in edge-list



;-------------------------------------------------------------------------------
(defn get-parcels-by-delivered [state parcel-map]
  "Retrieve parcels in 'parcel-list' whose :delivered key matches state "
  (let [parcel-map-vals (flatten (vec parcel-map))]
    (for [val parcel-map-vals
         :when (= state (:delivered val))]             ; get the 'delivered' val of each parcel and see if it matches 'state'
     val)))

#_(defn get-parcels-by-delivered-deprecated [state parcel-list]
   "Retrieve parcels in 'parcel-list' whose :delivered key matches state "
   (for [val parcel-list
         :when (= state (:delivered val))]             ; get the 'delivered' val of each parcel and see if it matches 'state'
     val))



;-------------------------------------------------------------------------------
(defn add-parcel-to-register [p-register parcel]             ; designed to be used with reduce function
  "Add the record 'parcel', to list of parcels 'p-register'"
  (let [{:keys [origin-room                                  ; destructure the parcel record to get contents
                dest-room
                content
                delivered]} parcel]
    
    (if delivered                                            ; decide where to put parcel based on delivery status
      
      (assoc p-register dest-room                            ; assoc new parcel list into the room's value 
                        (conj (get p-register dest-room)     ; append parcel to room's parcel list to create a new parcel list
                              parcel))
      
      (assoc p-register origin-room                          ; assoc new parcel list into the room's value 
                        (conj (get p-register origin-room)   ; append parcel to room's parcel list to create a new parcel list
                              parcel)))))



;-------------------------------------------------------------------------------
(defn add-all-parcels-to-register [p-register parcel-list]
  "Add all the parcel record in 'parcel-list' to the map 'p-register' "
  (reduce add-parcel-to-register
          p-register
          parcel-list))



;-------------------------------------------------------------------------------
(defn path-cost [graph path]
  "Get the total cost of list of nodes 'path' from start to finish in 'graph' "
  (if (> 2 (count path))
    0                                                ; 'path' length is zero, as it contains 1 or less nodes
    (loop [idx 0
           cost-sum 0]
      (let [curr-vert (get path idx)                 ; get the current node's key
            next-vert (get path (inc idx))           ; get the next node's key
            weight (get-in graph 
                           [curr-vert next-vert])]   ; drill into the map 'graph' to get 'weight' between 'curr-vert' & 'next-vert'
        
        (if next-vert                                ; check if 'next-vert' is nil & 'weight' was valid
          (recur (inc idx)                           ; increment 'idx' to move to next node
                 (+ cost-sum weight))                ; 'cost-sum' is current sum plus distance to next node
          
          cost-sum)))))                              ; 'next-vert' is nil, so we've reached last node in 'path', return the 'cost-sum'



;-------------------------------------------------------------------------------
; define a record to represent a parcel
(defrecord Parcel [origin-room            ; the room the parcel is originally in
                   dest-room              ; the destination the parcel is going to
                   content                ; the parcel's content
                   delivered])            ; whether the parcel has been delevered or not



;-------------------------------------------------------------------------------
; define a record to represent a robot
(defrecord Robot [positon               ; the current position of the robot
                  target-pos            ; the position the robot wishes to move to
                  max-parcels           ; the max ammout of parcels the robot can carry
                  parcels])             ; the parcels the robot is carrying



;-------------------------------------------------------------------------------
; create the 'building' from all the verticis listed in 'data' namespace
(def building (reduce alist-add-vertex {}
                      (into [] (concat data/corridor 
                                       data/outer-rooms 
                                       data/inner-rooms 
                                       data/special-rooms))))



;-------------------------------------------------------------------------------
; add all the edges to the to the 'building' graph
(def building (alist-add-all-edges building data/all-edges))



;-------------------------------------------------------------------------------
; define where all parcels currently are and where ther are to be delivered
;                        |origin|dest|content|delivered|
(def all-parcels [(Parcel. :r123 :r124 "dog" false)
                  (Parcel. :r125 :r126 "fish" false)
                  (Parcel. :r125 :r126 "fish" false)
                  (Parcel. :r127 :r128 "cat" false)
                  (Parcel. :r129 :r130 "hare" true)
                  (Parcel. :r131 :r132 "horse" true)])


;-------------------------------------------------------------------------------
; add all of the parcel infromation to the global-parcel-register
(def global-parcel-register (add-all-parcels-to-register {} all-parcels))



;-------------------------------------------------------------------------------
; init information about the simulated robot
(def robot {:pos nil :target-pos nil :max-parcels nil :parcels []})











;-------------------------------------------------------------------------------
;       WORKING AREA!

(def onePath [:c115])
(def zeroPath [])
(def path1 [:c131 :c129 :c127 :c125])
(def path2 [:ts :a2 :a3 :a1 :b1])
(def path3 [:c103 :b3 :b1 :c2 :c1 :c123])



(path-cost building zeroPath)
(def emptySeq [])

(conj emptySeq 9)

(reduce conj emptySeq '(:r123 :r444 :r234))

(last (reduce conj emptySeq '(:r123 :r444 :r234)))


;(clojure.pprint/pprint global-parcel-register)
