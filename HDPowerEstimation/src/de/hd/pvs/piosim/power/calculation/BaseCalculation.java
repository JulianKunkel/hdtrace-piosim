//	Copyright (C) 2010 Timo Minartz
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.
package de.hd.pvs.piosim.power.calculation;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * class to encapsulate all calculations. This is done because of
 * a global managing for the math context and the precision for each
 * operation on <code>BigDecimal</code> instances
 * @author Timo Minartz
 *
 */
public class BaseCalculation {
	
	private static MathContext dividePrecision = new MathContext(15);
	private static MathContext multiplyPrecision = new MathContext(15);
	
	public static final BigDecimal ONE = BigDecimal.ONE;
	public static final BigDecimal HUNDRED = new BigDecimal("100");
	public static final BigDecimal THOUSAND = new BigDecimal("1000");
	public static final BigDecimal ONE_MILLION = new BigDecimal("1000000");
	public static final BigDecimal TEN = new BigDecimal("10");
	
	public static MathContext getDividePrecision() {
		return dividePrecision;
	}

	public static void setDividePrecision(MathContext dividePrecision) {
		BaseCalculation.dividePrecision = dividePrecision;
	}

	public static MathContext getMultiplyPrecision() {
		return multiplyPrecision;
	}

	public static void setMultiplyPrecision(MathContext multiplyPrecision) {
		BaseCalculation.multiplyPrecision = multiplyPrecision;
	}

	public static BigDecimal sum(BigDecimal summand1, BigDecimal summand2) {
		return summand1.add(summand2);
	}
	
	public static BigDecimal sum(BigDecimal[] summands) {
		BigDecimal sum = new BigDecimal("0");
		for(BigDecimal summand : summands) {
			sum = sum(sum,summand);
		}
		return sum;
	}
	
	public static BigDecimal divide(BigDecimal base, BigDecimal divisor) throws ArithmeticException {
		if(divisor.doubleValue() == 0)
			throw new ArithmeticException("Divisor is zero!");
		return base.divide(divisor,dividePrecision);
	}
	
	public static BigDecimal substract(BigDecimal base, BigDecimal subtrahend) {
		return base.subtract(subtrahend);
	}
	
	public static BigDecimal multiply(BigDecimal base, BigDecimal multiplicand) {
		return base.multiply(multiplicand, multiplyPrecision);
	}
	

	public static BigDecimal interpolatePowerEfficiency(
			BigDecimal maxEfficiency, BigDecimal minEfficiency,
			BigDecimal utilization) {
		
		return sum(multiply(substract(maxEfficiency,minEfficiency),utilization),minEfficiency);
	}

	public static BigDecimal toNs(BigDecimal ms) {
		return BaseCalculation.multiply(ms, ONE_MILLION);
	}
	
	public static BigDecimal toMs(BigDecimal sec) {
		return BaseCalculation.multiply(sec, THOUSAND);
	}
	
	public static BigDecimal toByte(BigDecimal megaByteValue) {
		return BaseCalculation.multiply(BaseCalculation.multiply(megaByteValue, new BigDecimal("1024")), new BigDecimal("1024"));
	}

	public static BigDecimal toMegaByte(BigDecimal byteValue) {
		return BaseCalculation.divide(BaseCalculation.divide(byteValue, new BigDecimal("1024")), new BigDecimal("1024"));
	}

	public static BigDecimal toSec(BigDecimal msValue) {
		return BaseCalculation.divide(msValue, THOUSAND);
	}

	public static BigDecimal getAverage(BigDecimal[] utilization) {
		BigDecimal sum = sum(utilization);
		return BaseCalculation.divide(sum, new BigDecimal(utilization.length));
	}
	
	
}
