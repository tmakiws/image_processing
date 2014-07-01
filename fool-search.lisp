
(defstruct (feature (:conc-name ft-)) 
  (id "" :type string)
  (x  0.0)
  (y  0.0)
  (scl 0.0)
  (arg 0.0)
  (v (make-array 128
		 :element-type 'unsigned-byte
		 :adjustable nil
		 :fill-pointer nil)))

(defparameter *num-train-data* 45)
(defparameter *num-train-data/class* 15)

(defparameter features (make-array 0
			     :element-type 'feature
			     :adjustable t
			     :fill-pointer t))

(defmacro spl (str)
  `(read-from-string (concatenate 'string "(" ,str ")")))

(defmacro spl/byte (str)
  `(read-from-string (concatenate 'string "#(" ,str ")")))




(defun load-sifts ()
  (let ((root "../ssift/")
	(names '("sz/" "gs/" "md/")))
    (loop
       for dir in names
       do
	 (loop
	    for i from 1 to *num-train-data/class*
	    do
	      (with-open-file (in (format nil
					  "~a~a~a.keys"
					  root dir i)
				  :direction :input)
		(destructuring-bind (n d) #1=(spl (read-line in))
				    (declare (ignore d))
		  (loop repeat n
		     do
		       (destructuring-bind (x y scl arg) #1#
			 (let ((f (make-feature
				   :id dir
				   :x x :y y :scl scl :arg arg)))
			   (loop
			      for j from 0 below 8
			      for l = (spl/byte (read-line in))
			      do (loop
				    for k from 0 below 16
				    do
				      (setf (aref (ft-v f) (+ (ash j 4) k))
					    (aref l k))))
			   (vector-push-extend f features 1000)))
		       #1#)))))))


(defun dist (a b)
  (declare (type feature a b))
  (let ((av (ft-v a))
	(bv (ft-v b)))
    (loop
       for i from 0 below 128
       summing (abs (- (svref av i)
		       (svref bv i) )))))

(defun find-nearest (f fs)
  (loop
     with min = most-positive-fixnum
     with class = nil
     for tf across fs
     do
       (let ((d (dist f tf)))
	 (when (< d min)
	   (setf min d
		 class (ft-id tf))))
     finally
       (return (values class min))))

(defun fool-search (keysfile)
  (let ((fs nil)
	(counter (make-hash-table :test #'equal)))
    (with-open-file (in (format nil
			      keysfile)
		      :direction :input)
    (destructuring-bind (n d) #1=(spl (read-line in))
     (declare (ignore d))
     (setf fs (make-array n))
     (loop
	for i from 0 below n
	do
	  (destructuring-bind (x y scl arg) #1#
	    (let ((f (make-feature
		      :x x :y y :scl scl :arg arg)))
	      (loop
		 for j from 0 below 8
		 for l = (spl/byte (read-line in))
		 do (loop
		       for k from 0 below 16
		       do
			 (setf (aref (ft-v f) (+ (ash j 4) k))
			       (aref l k))))
	      (setf (aref fs i) f)))
	  #1#)))
    (loop
       for f across fs
       for nf = (find-nearest f features)
       do
	 (setf #2=(gethash nf counter 0)
	       (1+ #2#)))
    (loop
       for k being the hash-keys in counter
       do
	 (format t "~a : ~a~%" k (gethash k counter)))))
