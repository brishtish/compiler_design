package tac; 


public abstract class TACInstr {

   
    public static class TACBinOp extends TACInstr {
        public final String dest;  
        public final String left;  
        public final String op;    
        public final String right; 

       
        public TACBinOp(String dest, String left, String op, String right) {
            this.dest = dest;   
            this.left = left;   
            this.op = op;       
            this.right = right; 
        }

       
        @Override
        public String toString() {
            return String.format("%s = %s %s %s", dest, left, op, right);
        }
    }

   
    public static class TACCopy extends TACInstr {
        public final String dest; 
        public final String src;  

       
        public TACCopy(String dest, String src) {
            this.dest = dest; 
            this.src = src;  
        }

        
        @Override
        public String toString() {
            return String.format("%s = %s", dest, src);
        }
    }

   
    public static class TACPrint extends TACInstr {
        public final String src; 

        
        public TACPrint(String src) {
            this.src = src; 
        }

        
        @Override
        public String toString() {
            return String.format("print %s", src);
        }
    }

   
    public static class TACLabel extends TACInstr {
        public final String label; 

      
        public TACLabel(String label) {
            this.label = label; 
        }

        
        @Override
        public String toString() {
            return String.format("%s:", label);
        }
    }

   
    public static class TACJump extends TACInstr {
        public final String target; 

        
        public TACJump(String target) {
            this.target = target; 
        }

        
        @Override
        public String toString() {
            return String.format("goto %s", target);
        }
    }

    
    public static class TACJumpIfFalse extends TACInstr {
        public final String condition; 
        public final String target;   

        
        public TACJumpIfFalse(String condition, String target) {
            this.condition = condition; 
            this.target = target;       
        }

        
        @Override
        public String toString() {
            return String.format("ifFalse %s goto %s", condition, target);
        }
    }
}
