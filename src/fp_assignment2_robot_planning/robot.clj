(ns fp-assignment2-robot-planning.robot
  (use [fp-assignment2-robot-planning.core]))

;-------------------------------------------------------------------------------
; define a record to represent a parcel
(defrecord Parcel [origin-room            ; the room the parcel is originally in
                   dest-room              ; the destination the parcel is going to
                   content                ; the parcel's content
                   delivered])            ; whether the parcel has been delevered or not


;-------------------------------------------------------------------------------
; define a record to represent a robot
(defrecord Robot [graph          ; the graph which represents the building plan
                  curr-pos       ; the current position of the robot
                  parcels           ; the parcels the robot is carrying
                  parcel-map        ; the information about where parcels are in the building
                  curr-path      ; the path the the robot is on currently
                  curr-path-idx])   ; the index of 'current-pos' in 'current-path'







;-------------------------------------------------------------------------------
; define where all parcels currently are and where ther are to be delivered
;                        |origin|dest|content|delivered|
(def all-parcels [(Parcel. :r123 :r125 "dog" false)
                  (Parcel. :r125 :r127 "fish" false)
                  (Parcel. :r125 :r115 "lion" false)
                  (Parcel. :r125 :r113 "zeebra" false)
                  (Parcel. :r131 :r111 "horse" true)
                  (Parcel. :r131 :r101 "cat" true)
                  (Parcel. :r125 :r103 "mouse" true)
                  (Parcel. :r129 :r107 "cow" true)])



;-------------------------------------------------------------------------------
; define the initial position of the robot
(def initial-pos :r125 )



;-------------------------------------------------------------------------------
; init information about the simulated robot
(def ROBOT (Robot. building
                   initial-pos                         ; set initial position of robot, defined above
                   nil                                 ; parcels list starts out empty
                   (add-parcels-to-reg {} all-parcels) ; add all the parcels in 'all-parcels' to the robot's parcel map
                   []                                  ; robot's path is initially empty
                   nil))                               ; not on a path so current index initialised to nil

(pp/pprint (pickup-parcels ROBOT))

;-------------------------------------------------------------------------------
(defn pickup-parcels [robot]
  "Picks up all parcels at the robot's current location, updates parcel list and parcel map accordingly"
  (let [robot-pos (:curr-pos robot)                        ; get the robot's current positon
        parcel-map (:parcel-map robot)                     ; get the robot's parcel-map
        parcels-pos (robot-pos parcel-map)                 ; use robot-pos to get all the parcels in the map at that position
        
        undelivered (get-parcels-by-delivered false        ; get all the undelivered parcels at the current position
                                              parcels-pos)
        remaining (get-parcels-by-delivered true           ; get all the remaining parcels that have been delivered already
                                              parcels-pos)
        parcel-map* (assoc parcel-map robot-pos remaining) ; create a parcel map with collected parcels in 'undelivered' removed
        parcels (:parcels robot)                           ; get the robot's original parcel list
        
        robot+ (assoc robot :parcels (apply conj           ; update the original 'robot' parcel list (parcels) with the list... 
                                            parcels        ; of undelivered parcels in 'undelivered'
                                            undelivered))   
        
        robot++ (assoc robot+ :parcel-map parcel-map*)]    ; update the modified 'robot+' parcel-map to change it to the...
                                                           ; updated version 'parcel-map*'
                                                           
    robot++))                                              ; return the newly updated robot


(defn cost-to-pdest [pos parcel graph]
  "Calculate the cost of travelling from 'pos' to destination of 'parcel'"
  (let [dest (:dest-room parcel)
        path (plan-path graph pos dest)
        dist (path-cost graph path)]
    dist))



(defn dropoff-parcels [robot]
  (let ))



(cost-to-pdest :mail (get all-parcels 4) building)

(pp/pprint (weigh-parcels :a1 all-parcels building))

(def wmap (weigh-parcels :a1 all-parcels building))
(flatten wmap)
(reduce )
                            
(defn weigh-parcels [pos parcels graph]
  ""
  (let [weight-map1 (for [p parcels                      ; create first stage of the weight map, putting parcels into...
                          :let [dist (cost-to-pdest pos  ; seqs with the cost of a path from 'pos' to 'p'
                                                    p 
                                                    graph)]]
                      [p dist])
        weight-map2 ()]
    weight-map1))


(pp/pprint (get-parcels-by-delivered true (:parcel-map ROBOT)))

