package eva2.server.go.enums;

public enum PSOTopologyEnum {
    /**
    *
    */
   linear,
   /**
    *
    */
   grid,
   /**
    *
    */
   star,
   /**
    *
    */
   multiSwarm,
   /**
    *
    */
   tree,
   /**
    *
    */
   hpso,
   /**
    *
    */
   random; 
   
   /**
    * A method to translate the "old" integer tags into the enum type.
    * @param oldID
    * @return
    */
   public static PSOTopologyEnum translateOldID(int oldID) {
	   switch (oldID) {
	   case 0: return linear;
	   case 1: return grid;
	   case 2: return star;
	   case 3: return multiSwarm;
	   case 4: return tree;
	   case 5: return hpso;
	   case 6: return random;
	   }
	   return random;
   }
}
