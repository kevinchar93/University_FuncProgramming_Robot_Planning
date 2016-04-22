(ns fp-assignment2-robot-planning.core
  (require [fp-assignment2-robot-planning.data :as data])
  (require [clojure.data.priority-map :as pm])
  (require [clojure.pprint :as pp]))

;-------------------------------------------------------------------------------
(defn alist-add-vertex [adj-list vertex-name]
  "Add vertex 'vertex-name' with no edges to adjacency list 'adj-list'"
  
  (conj adj-list  [vertex-name {}]))



;-------------------------------------------------------------------------------
(defn alist-add-edge [adj-list vert-a vert-b weight]
  "Add a weighted edge between two verticies in the adjacency list"
  
  (if (and (contains? adj-list vert-a)               ; check if the list has both veritcies to begin with
           (contains? adj-list vert-b)) 
    
    (assoc-in (assoc-in adj-list                     ; TRUE - associate verticies with one another, give each the same weight
                        [vert-a vert-b] 
                        weight)
              [vert-b vert-a]
              weight)
    adj-list))                                       ; FALSE - return the list if the verticies are not present



;-------------------------------------------------------------------------------
(defn alist-add-all-edges [adj-list edges]
  "Add all of the edges in the given list 'edges' to 'adj-list'"
  
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
(defn parcels-by-deliv [state parcel-map]
  "Retrieve parcels in 'parcel-list' whose :delivered key matches state"
  
  (let [parcel-map-vals (flatten (vec parcel-map))]
    (for [val parcel-map-vals
         :when (= state (:delivered val))]             ; get the 'delivered' val of each parcel and see if it matches 'state'
     val)))



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
(defn add-parcels-to-reg [p-register parcel-list]
  "Add all the parcel record in 'parcel-list' to the map 'p-register'"
  
  (reduce add-parcel-to-register
          p-register
          parcel-list))



;-------------------------------------------------------------------------------
(defn path-cost [graph path]
  "Get the total cost of list of nodes in 'path', from start to finish in 'graph'"
  
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
  "Check if a map does not contain the key"
  
  (not (contains? coll key)))



;-------------------------------------------------------------------------------
(defn contains!-seq [coll key]
  "Check if the sequence contains at least one of the key"
  
  (not (some #(= key %) coll)))



;-------------------------------------------------------------------------------
(defn expand-path [frontier explored path node actions graph]
  "Take the given 'path' and expand it using connected nodes in 'graph', 
    return frontier with new paths added from expansion of the given path"
  
  (loop [new-frontier frontier
         idx 0]
    
    (let [next-pos (get actions idx)            ; get a next positon from the action list
          path+ (conj path next-pos)            ; generate new path that would be created if 'next-pos' was added to 'path'
          p-cost (path-cost graph path+)        ; calculate the total cost of this new path
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



;-------------------------------------------------------------------------------
(defn plan-path [graph begin goal]
  "Attempt to find a route in 'graph' from 'begin' to 'goal'
   using an implementation of the uniform cost search algorithm"
  
  (loop [frontier (pm/priority-map [begin] 0 ); init frontier as 'begin' node with cost 0
         explored #{}]                        ; set of expored nodes initially set to empty
    ( if (= 0 (count frontier)) 
      false                                   ; path not possible
      
      (let [[path _] (first frontier)         ; get path with shortest weight
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



;-------------------------------------------------------------------------------
; create the 'building' from all the verticis listed in 'data' namespace
(def building (reduce alist-add-vertex {}
                      (into [] data/all-rooms)))



;-------------------------------------------------------------------------------
; add all the edges to the to the 'building' graph
(def building (alist-add-all-edges building data/all-edges))






