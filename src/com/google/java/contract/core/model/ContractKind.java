/*
 * Copyright 2007 Johannes Rieken
 * Copyright 2010 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package com.google.java.contract.core.model;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;

/**
 * The kind of a contract method. This information is at the bytecode
 * level. There are more contract method kinds than there are
 * annotations; some of them, such as {@link #OLD} and
 * {@link #SIGNAL_OLD} are used for internal purposes.
 *
 * @author nhat.minh.le@huoc.org (Nhat Minh Lê)
 */
public enum ContractKind {
  /**
   * A precondition contract method, which evaluates all direct
   * (non-inherited) preconditions of the target method.
   */
  PRE,

  /**
   * A postcondition contract method, which evaluates all direct
   * (non-inherited) postconditions of the target method.
   */
  POST,

  /**
   * An exceptional postcondition contract method, which evaluates all
   * direct (non-inherited) exceptional postconditions of the target
   * method.
   */
  SIGNAL,

  /**
   * An invariant contract method, which evaluates all direct
   * (non-inherited) invariants of the target class.
   */
  INVARIANT,

  /**
   * An old value contract method, which computes one old value
   * expression for the corresponding postcondition contract method.
   */
  OLD,

  /**
   * An exceptional old value contract method, which computes one old
   * value expression for the corresponding exceptional postcondition
   * contract method.
   */
  SIGNAL_OLD,

  /**
   * A synthetic access method, generated by the Java compiler, used
   * in contract methods.
   */
  ACCESS,

  /**
   * A synthetic lambda method, generated by the Java compiler, used
   * in contract methods.
   */
  LAMBDA,

  /**
   * A contract helper, for indirect contract evaluation.
   *
   * @see ContractCreation
   */
  HELPER;

  /**
   * Returns {@code true} if this kind denotes a contract method that
   * applies to a class.
   */
  @Ensures("result == (!isMethodContract() && !isHelperContract())")
  public boolean isClassContract() {
    switch (this) {
      case INVARIANT:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns {@code true} if this kind denotes a contract method that
   * applies to a method.
   */
  @Ensures("result == (!isClassContract() && !isHelperContract())")
  public boolean isMethodContract() {
    switch (this) {
      case PRE:
      case POST:
      case SIGNAL:
      case OLD:
      case SIGNAL_OLD:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns {@code true} if this kind denotes a helper contract
   * method, called by other contract methods.
   */
  @Ensures("result == (!isClassContract() && !isMethodContract())")
  public boolean isHelperContract() {
    return !isClassContract() && !isMethodContract();
  }

  /**
   * Returns {@code true} if this kind denotes a postcondition (normal
   * or exceptional).
   */
  @Ensures({
    "!result || isMethodContract()",
    "!(result && isOld())"
  })
  public boolean isPostcondition() {
    switch (this) {
      case POST:
      case SIGNAL:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns {@code true} if this kind denotes a method contract old
   * computing method values.
   */
  @Ensures({
    "!result || isMethodContract()",
    "!(result && isPostcondition())"
  })
  public boolean isOld() {
    switch (this) {
      case OLD:
      case SIGNAL_OLD:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns the kind of the old value contract methods computing old
   * values for this kind.
   */
  @Requires("isPostcondition()")
  public ContractKind getOldKind() {
    switch (this) {
      case POST:
        return OLD;
      case SIGNAL:
        return SIGNAL_OLD;
      default:
        throw new IllegalArgumentException();
    }
  }

  public boolean hasNameSpace() {
    return this != HELPER;
  }

  /**
   * Returns the name space used when generating contract method names
   * for this kind.
   */
  @Requires("hasNameSpace()")
  @Ensures("ClassName.isSimpleName(result)")
  public String getNameSpace() {
    switch (this) {
      case PRE:
        return "com$google$java$contract$P";
      case POST:
        return "com$google$java$contract$Q";
      case SIGNAL:
        return "com$google$java$contract$E";
      case INVARIANT:
        return "com$google$java$contract$I";
      case OLD:
        return "com$google$java$contract$QO";
      case SIGNAL_OLD:
        return "com$google$java$contract$EO";
      case ACCESS:
        return "access";
      case LAMBDA:
        return "lambda";
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns the name space used when generating helper contract
   * method names for this kind.
   */
  @Requires("!isHelperContract()")
  @Ensures("ClassName.isSimpleName(result)")
  public String getHelperNameSpace() {
    switch (this) {
      case PRE:
        return "com$google$java$contract$PH";
      case POST:
        return "com$google$java$contract$QH";
      case SIGNAL:
        return "com$google$java$contract$EH";
      case INVARIANT:
        return "com$google$java$contract$IH";
      case OLD:
        return "com$google$java$contract$QOH";
      case SIGNAL_OLD:
        return "com$google$java$contract$EOH";
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Returns the variance of this kind, or {@code null} if not
   * applicable.
   */
  public ContractVariance getVariance() {
    switch (this) {
      case PRE:
        return ContractVariance.CONTRAVARIANT;
      case POST:
      case SIGNAL:
      case INVARIANT:
        return ContractVariance.COVARIANT;
      default:
        return null;
    }
  }
}
