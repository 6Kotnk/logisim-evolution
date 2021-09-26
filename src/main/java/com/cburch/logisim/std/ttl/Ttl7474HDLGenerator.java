/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.ttl;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHDLGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.fpga.hdlgenerator.HDLPorts;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;

public class Ttl7474HDLGenerator extends AbstractHDLGeneratorFactory {

  public Ttl7474HDLGenerator() {
    super();
    myWires
        .addWire("state1", 1)
        .addWire("state2", 1)
        .addWire("next1", 1)
        .addWire("next2", 1);
    myPorts
        .add(Port.CLOCK, HDLPorts.getClockName(1), 1, 2)
        .add(Port.CLOCK, HDLPorts.getClockName(2), 2, 9)
        .add(Port.INPUT, "nCLR1", 1, 0)
        .add(Port.INPUT, "D1", 1, 1)
        .add(Port.INPUT, "nPRE1", 1, 3)
        .add(Port.INPUT, "nCLR2", 1, 11)
        .add(Port.INPUT, "D2", 1, 10)
        .add(Port.INPUT, "nPRE2", 1, 8)
        .add(Port.OUTPUT, "Q1", 1, 4)
        .add(Port.OUTPUT, "nQ1", 1, 5)
        .add(Port.OUTPUT, "Q2", 1, 7)
        .add(Port.OUTPUT, "nQ2", 1, 6);
  }

  @Override
  public ArrayList<String> GetModuleFunctionality(Netlist theNetlist, AttributeSet attrs) {
    final var contents = new LineBuffer();
    return contents
        .pair("CLK1", HDLPorts.getClockName(1))
        .pair("CLK2", HDLPorts.getClockName(2))
        .pair("tick1", HDLPorts.getTickName(1))
        .pair("tick2", HDLPorts.getTickName(2))
        .add("""
            Q1  <= state1;
            nQ1 <= NOT(state1);
            Q2  <= state1;
            nQ2 <= NOT(state1);
            
            next1 <= D1 WHEN {{tick1}}='1' ELSE state1;
            next2 <= D2 WHEN {{tick2}}='1' ELSE state2;
            
            ff1 : PROCESS ( {{CLK1}} , nCLR1 , nPRE1 ) IS
               BEGIN
                  IF (nCLR1 = '0') THEN state1 <= '0';
                  ELSIF (nPRE1 = '1') THEN state1 <= '1';
                  ELSIF (rising_edge({{CLK1}})) THEN state1 <= next1;
                  END IF;
               END PROCESS ff1;
            
            ff2 : PROCESS ( {{CLK2}} , nCLR2 , nPRE2 ) IS
               BEGIN
                  IF (nCLR2 = '0') THEN state2 <= '0';
                  ELSIF (nPRE2 = '1') THEN state2 <= '1';
                  ELSIF (rising_edge({{CLK2}})) THEN state2 <= next2;
                  END IF;
               END PROCESS ff2;
           """)
        .getWithIndent();
  }

  @Override
  public boolean isHDLSupportedTarget(AttributeSet attrs) {
    /* TODO: Add support for the ones with VCC and Ground Pin */
    if (attrs == null) return false;
    return (!attrs.getValue(TtlLibrary.VCC_GND) && HDL.isVHDL());
  }
}
