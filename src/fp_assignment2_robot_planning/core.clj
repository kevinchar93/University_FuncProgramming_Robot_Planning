(ns fp-assignment2-robot-planning.core
  (require [fp-assignment2-robot-planning.data :as data])
  (require [clojure.data.priority-map :as pm])
  (require [clojure.pprint :as pp]))


(def clojure.pprint/pprint pp)
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
(defn get-actions [graph node]
  "Get all places that can be moved to in 'graph' from position of 'node'"
  (vec (keys                   ; get the keys which are all positions connected to 'node'
         (get graph node))))   ; get the map representing other nodes connected to 'node'



;-------------------------------------------------------------------------------
(defn contains! [coll key]
  ""
  (not (contains? coll key)))



;-------------------------------------------------------------------------------
(defn contains!-seq [coll key]
  ""
  (not (some #(= key %) coll)))



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






(defn replace-w-shorter [frontier path graph]
  "Replace equivalent paths in the frontier that are longer than 'path'"
  (let [cost (path-cost graph path)                        ; get the cost of the given path
        equivs (for [c frontier                            ; get all paths in 'frontier' that end on same node as 'path' (equivalents)
                    :when (= (last path) (last c))]
                c)
        longer-paths (for [c equivs                        ; get all paths that cost more than 'path' from 'equivs'
                           :when (<= cost (path-cost c))]
                       c)
        frontier- (dissoc frontier longer-paths)]          ; generate what the frontier would be with longer paths removed
    
    (if (empty? longer-paths)                              ; check if there are any equivalent paths that are longer
      frontier                                             ; if not the frontier is unchanged
      (assoc frontier- path cost))))                       ; if so return the modified 'frontier-' with the new path replacing
                                                           ; all the longer paths

                                                           
                                                           
                                                           
                                                           
                                                           
                                                           
(plan-path building :mail :r125)


(defn plan-path [graph begin goal]
  "Attempt to find a route in 'graph' from 'begin' to 'goal' "
  (loop [frontier (pm/priority-map [begin] 0 ); init frontier as 'begin' node with cost 0
         explored #{}]                        ; set of expored nodes initially set to empty
    ( if (= 0 (count frontier)) 
      false                                   ; path not possible
      
      (let [[path _]     (first frontier)     ; get path with shortest weight
           path-end  (last path)              ; get node at the end of the path 
           explored+ (conj explored           ; the explored set but with the new node 'path-end' added
                           path-end)   
           frontier- (dissoc frontier path)   ; the frontier with the current path removed from it
           actions   (get-actions graph       ; all the moves that can be made from the end of the current path
                                  path-end)
           frontier* (expand-path frontier-   ; expand the end of the current path to create a new updated frontier
                        explored+
                        path
                        path-end
                        actions
                        graph)]   
        
        (if (= path-end goal)                 ; check to see if we've reached the goal
          path                                ; goal was reached - return this path
          
          (recur frontier*                    ; goal not reached recur with updated frontier & explored set
                 explored+))))))



(def pmap (pm/priority-map :a 2 :b 1 :c 3 :d 5 :e 4 :f 3))
(first (keys pmap))
(let [[[path] _ ] (first frontier)]
  path)
(def begin :c131)
(def frontier (pm/priority-map [begin] 0 ))
(def path (first))
(def frontier- (pm/priority-map [begin] 0 ))

(expand-path )


(defn expand-path [frontier explored path node actions graph]
  (loop [new-frontier frontier
         idx 0]
    
    (let [next-pos (get actions idx)            ; get a next positon from the action list
          path+ (conj path next-pos)            ; generate new path that would be created if 'next-pos' was added to 'path'
          p-cost (path-cost graph path+)         ; calculate the total cost of this new path
          ends (for [c new-frontier]            ; get all the nodes on the end of the paths in the frontier
                 (last c))
          frontier+ (assoc new-frontier         ; generate frontier that would be created if path+ was added to frontier
                           path+
                           p-cost)
          idx+ (inc idx)]                       ; the next index value
      (if next-pos                              ; check if nil, if so we've added all the expanded paths
        
        (if (and (contains! explored next-pos)  ; if the 'next-pos' is not in the explored set 
                 (contains!-seq ends next-pos)) ; and not in the ends of the frontier
          (recur frontier+ idx+)                ; recur with the new frontier and the incremented index
          
          (recur (replace-w-shorter frontier    ; otherwise check to see if the new path can replace equivalent
                                    path+       ; onger paths
                                    graph)
                 idx+))
        
        new-frontier))))                        ; return the newly update frontier




(defn expand-path [frontier explored path node actions graph]
  (loop [new-frontier frontier
         idx 0]
    
    (let [next-pos (get actions idx)            ; get a next positon from the action list
          path+ (conj path next-pos)            ; generate new path that would be created if 'next-pos' was added to 'path'
          p-cost (path-cost graph path+)         ; calculate the total cost of this new path
          ends (for [c new-frontier]            ; get all the nodes on the end of the paths in the frontier
                 (last c))
          frontier+ (assoc new-frontier         ; generate frontier that would be created if path+ was added to frontier
                           path+
                           p-cost)
          idx+ (inc idx)]                       ; the next index value
      (if next-pos                              ; check if nil, if so we've added all the expanded paths
        
        (if (and (contains! explored next-pos)  ; if the 'next-pos' is not in the explored set 
                 (contains!-seq ends next-pos)) ; and not in the ends of the frontier
          (recur frontier+ idx+)                ; recur with the new frontier and the incremented index
          
          (recur new-frontier idx+))            ; recure with unchaged frontier and incremented index
        
        new-frontier))))                        ; return the newly update frontier






(def onePath [:c115])
(def zeroPath [])
(def path1 [:c131 :c129 :c127 :c125])
(def path2 [:ts :a2 :a3 :a1 :b1])
(def path3 [:c103 :b3 :b1 :c2 :c1 :c123])
(def path4 [:c119 :c121 :c123])
(def path5 [:c117 :c118B :c118A :c119 :c121 :c123])
(def path6 [:c103 :b3 :b1 :c2 :c1 :c123 :c125])


(def demopaths '([:c131 :c129 :c127 :c125]
                 [:ts :a2 :a3 :a1 :b1]
                 [:c103 :b3 :b1 :c2 :c1 :c123]
                 [:c119 :c121 :c123]
                 [:c117 :c118B :c118A :c119 :c121 :c123]
                 [:c103 :b3 :b1 :c2 :c1 :c123 :c125]))


(clojure.pprint/pprint mappedvals)
