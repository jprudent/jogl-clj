(ns jogl-clj.core
  (:import (javax.media.opengl GLEventListener GLCapabilities GLProfile
                               GLAutoDrawable GL2 GL GL2ES1 DebugGL2 GL3)
           (com.jogamp.opengl.util Animator FPSAnimator)
           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.newt.event WindowAdapter WindowListener)
           (javax.media.nativewindow WindowClosingProtocol$WindowClosingMode)
           (javax.media.opengl.glu GLU)
           (javax.media.opengl.fixedfunc GLMatrixFunc GLLightingFunc)))

(defn gl-capabilities-factory  []
  (doto (GLCapabilities. (GLProfile/getDefault))))

(defn gl-window-factory [capabilities glEventListener animator]
  (doto (GLWindow/create capabilities)
    (.addGLEventListener glEventListener)
    (.addWindowListener (proxy [WindowAdapter] []
                          (window-destroyed [_]
                            (println "closing window and stopping animator")
                            (.stop animator))))
    (.setTitle "title")
    (.setSize 200 200)
    (.setUndecorated false)
    (.setPointerVisible true)
    (.setVisible true)
    (.setFullscreen false)
    (.setDefaultCloseOperation WindowClosingProtocol$WindowClosingMode/DISPOSE_ON_CLOSE)))

(defn animator-factory [capabilities glEventListener]
  (let [animator (FPSAnimator. 1)
        window (gl-window-factory capabilities glEventListener animator)]
    (doto animator (.add window))))

(defn- gl [drawable]
  (-> drawable .getGL .getGL2 (DebugGL2.)))

(defn gl-event-listener-factory
  [^GLU glu]
  (reify GLEventListener

    (init [_ drawable]
      (doto (gl drawable)
        (.glClearColor 0 0 0 0)
        (.glClearDepth 1)
        (.glEnable GL/GL_DEPTH_TEST)
        (.glDepthFunc GL/GL_LEQUAL)
        (.glHint GL2ES1/GL_PERSPECTIVE_CORRECTION_HINT, GL/GL_NICEST)
        (.glShadeModel GLLightingFunc/GL_SMOOTH))
      nil)

    (dispose [_ _] nil)

    (display [_ drawable]
      (doto (gl drawable)
        (.glClear (bit-or GL/GL_COLOR_BUFFER_BIT GL/GL_DEPTH_BUFFER_BIT))
        (.glLoadIdentity)
        (.glTranslatef 0 0 -6)
        (.glBegin GL/GL_TRIANGLES)
        (.glVertex3f 0 1 0)
        (.glVertex3f -1 -1 0)
        (.glVertex3f 1 -1 0)
        (.glEnd)) nil)

    (reshape [_ drawable _ _ width height]
      (let [height (if (zero? height) 1 height)
            gl (gl drawable)]
        (doto gl
          (.glViewport 0 0 width height)
          (.glMatrixMode GLMatrixFunc/GL_PROJECTION)
          (.glLoadIdentity))
        (.gluPerspective glu 45. (double (/ width height)) 0.1 100.)
        (doto gl (.glMatrixMode GLMatrixFunc/GL_MODELVIEW)
                 (.glLoadIdentity))))))

(def game (animator-factory
                (gl-capabilities-factory)
                (gl-event-listener-factory (GLU.))))
(.start game)