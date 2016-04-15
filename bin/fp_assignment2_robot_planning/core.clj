(ns fp-assignment2-robot-planning.core)
(require '[fp-assignment2-robot-planning.data :as data])

; add new vertex to a given adjacency list
(defn alist-add-vertex [adj-list vertex-name]
  "Add a vertex with no edges to the given adjacency list"
  (conj adj-list  [vertex-name {}]))



; add a weighted edge between two verticies in the adjacency list
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


; create the building from all the verticies listed above
(def building (reduce alist-add-vertex {}
                      (into [] (concat data/corridor data/outer-rooms data/inner-rooms data/special-rooms))))


; add all the items from the edge list to the graph
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


(alist-add-all-edges building data/all-edges)
