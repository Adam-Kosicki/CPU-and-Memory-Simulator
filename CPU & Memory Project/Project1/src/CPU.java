import java.io.*;

public class CPU {
    final static int memory_bound = 1000;
    static BufferedReader input = null;
    static BufferedWriter output = null;
    static Process process1 = null;
    public String val;
    private boolean user_mode = true;
    private boolean exit = false;
    public boolean flag;
    private int X = 0;
    private int Y = 0;
    private int SP = 1000;
    private int AC = 0;
    private int PC = 0;
    private int IR = 0;
    private int timer = 0;
    private static int time = 0;
    private static CPU processor1 = new CPU();                                        //CPU object created


    public int read(int addr) throws IOException {
        output.write(String.format("%d\n\n", addr));
        output.flush();
        val = input.readLine();
        return Integer.parseInt(val);
    }

    public void write(int addr, int val) throws IOException {
        output.write(String.format("%d %d\n\n", addr, val));
        output.flush();
    }

    private void loop() throws IOException {
        flag = false;
        IR = read(PC);
        if (IR == 1 || IR == 2 || IR == 3 || IR == 4 || IR == 5 || IR == 7
                || IR == 9 || IR == 20 || IR == 21 || IR == 22 || IR == 23) {
            flag = true;
        }

        if (flag) {
            int opcode = read(++PC);
            checkAccess(opcode);
            run(IR, opcode);
        } else {
            run(IR, 0);
        }
        PC++;
    }

    public void execute() throws IOException {
        while (true) {
            loop();
            if (exit) {
                break;
            }
        }
    }

    private void run(int instr, int opcode) throws IOException {
        // Contains all the instructions that the CPU can execute
        switch (instr) {
            case 1: // Load the value into the AC
                this.AC = opcode;
                break;

            case 2: // Load the value at the address into the AC
                checkAccess(opcode);
                this.AC = read(opcode);
                break;

            case 3: // Load the value from the address found in the given address into the AC
                // (for example, if LoadInd 500, and 500 contains 100, then load from 100).
                checkAccess(opcode);
                checkAccess(read(opcode));
                this.AC = read(read(opcode));
                break;

            case 4: // Load the value at (address+X) into the AC
                // (for example, if LoadIdxX 500, and X contains 10, then load from 510).
                checkAccess(opcode + this.X);
                this.AC = read(opcode + this.X);
                break;

            case 5: // Load the value at (address+Y) into the AC
                checkAccess(opcode + this.Y);
                this.AC = read(opcode + this.Y);
                break;

            case 6: // Load from (Sp+X) into the AC (if SP is 990, and X is 1, load from 991).
                checkAccess(this.SP + this.X);
                this.AC = read(this.SP + this.X);
                break;

            case 7: // Store the value in the AC into the address
                checkAccess(opcode);
                write(opcode, this.AC);
                break;

            case 8: // Gets a random int from 1 to 100 into the AC
                this.AC = (int) (Math.random() * (100) + 1);
                break;

            case 9: // If port=1, writes AC as an int to the screen
                // If port=2, writes AC as a char to the screen
                if (opcode == 1)
                    System.out.print(AC);
                else if (opcode == 2)
                    System.out.print((char) AC);
                break;

            case 10: // Add the value in X to the AC
                this.AC += this.X;
                break;

            case 11: // Add the value in Y to the AC
                this.AC += this.Y;
                break;

            case 12: // Subtract the value in X from the AC
                this.AC -= this.X;
                break;

            case 13: // Subtract the value in Y from the AC
                this.AC -= this.Y;
                break;

            case 14: // Copy the value in the AC to X
                this.X = this.AC;
                break;

            case 15: // Copy the value in X to the AC
                this.AC = this.X;
                break;

            case 16: // Copy the value in the AC to Y
                this.Y = this.AC;
                break;

            case 17: // Copy the value in Y to the AC
                this.AC = this.Y;
                break;

            case 18: // Copy the value in AC to the SP
                this.SP = this.AC;
                break;

            case 19: // Copy the value in SP to the AC
                this.AC = this.SP;
                break;

            case 20: // Jump to the address
                this.PC = opcode - 1;
                break;

            case 21: // Jump to the address only if the value in the AC is zero
                if (AC == 0) {
                    PC = opcode - 1;
                }
                break;

            case 22: // Jump to the address only if the value in the AC is not zero
                if (AC != 0) {
                    PC = opcode - 1;
                }
                break;

            case 23: // Push return address onto stack, jump to the address
                SP--;
                write(SP, ++PC);
                PC = opcode - 1;
                break;

            case 24: // Pop return address from the stack, jump to the address
                PC = read(SP) - 1;
                SP++;
                break;

            case 25: // Increment the value in X
                X++;
                break;

            case 26: // Decrement the value in X
                X--;
                break;

            case 27: // Push AC onto stack
                SP--;
                write(SP, AC);
                break;

            case 28: // Pop from stack into AC
                AC = read(SP);
                SP++;
                break;

            case 29: // Perform system call
                if (user_mode) {
                    user_mode = false;
                    write(1999, SP);
                    write(1998, ++PC);
                    write(1997, AC);
                    write(1996, X);
                    write(1995, Y);
                    SP = 1995;
                    PC = 1499;
                }
                break;

            case 30: // Return from system call
                if (SP <= 1999) {
                    Y = read(1995);
                    X = read(1996);
                    AC = read(1997);
                    PC = read(1998) - 1;
                    SP = read(1999);
                    user_mode = true;
                } else {
                    // When system stack is empty
                    System.out.println("Error-- Stack Empty...");
                    process1.destroy(); // Kills process
                    System.exit(1);
                }
                break;

            case 50: // End execution
                output.write("exit");
                exit = true;
                break;

            default: // Else if instruction is incorrect, runs default
                System.out.println("Invalid Instruction-- Program Counter: " + PC + " \n\n Instruction Register: " + IR + "...");
                System.exit(0);
                break;
        }

        timer++; // Timer incremented after each instruction
        if (timer == time) { // System goes to system mode after timer is reached
            if (user_mode) {
                // Register copied to stack
                write(1999, SP);
                write(1998, ++PC);
                write(1997, AC);
                write(1996, X);
                write(1995, Y);
                SP = 1995;
                PC = 999;
                user_mode = false;
            }
            timer = 0;
        }
    }

    private void checkAccess(int addr) {
        if (user_mode && (addr < 0 || addr >= memory_bound)) {
            System.out.println("Denied Memory Access...");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            // First argument is the path
            process1 = Runtime.getRuntime().exec("java Memory " + args[0]);
            input = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream()));

            // Second argument is the timer
            time = Integer.parseInt(args[1]);

            processor1.execute();

            // Close input and output streams
            input.close();
            output.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}


