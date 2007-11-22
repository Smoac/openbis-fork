/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.exceptions;

/**
 * Exception representing internal error in the program caused by programmer error. It should never be caught. <br>
 * <br>
 * This class becomes handy when we do comparition of some value with the enumerator. The following 'enum-match' pattern
 * should be applied: 1. all values of the enumerator should be checked 2. if no matching of the value and enumerator is
 * found, it is a programmer mistake and internal error occurs. It means that when one adds a new enumerator value, one
 * should check every comparition of other values and update them.
 * 
 * <pre>
 * Example:
 * enum TypeX { A, B, C }
 *  
 * void doSomething(TypeX value) {
 *   if (value.equals(TypeX.A) {
 *      // process A
 *   } else if (value.equals(TypeX.B) {
 *      // process C
 *   } else if (value.equals(TypeX.C) {
 *      // process C
 *   } else
 *     throw InternalErr.error();
 * }
 * </pre>
 * 
 * @author Tomasz Pylak on Sep 3, 2007
 */
// TODO 2007-11-22, Christian Ribeaud: remove this class as, when used, it did not give any clue about what the problem
// is. Additionally, if assertions are disabled, you will get an exception with a stack trace with does not match
// the current state. Instead of using this class, we should use 'assert condition : cause' or throw a fresh new
// IllegalArgumentException.
public class InternalErr extends RuntimeException
{
    private static final InternalErr instance = new InternalErr();

    private static final long serialVersionUID = 1L;

    private InternalErr()
    {
    }

    public static final RuntimeException error()
    {
        assert false : "This should never happen";
        return instance;
    }
}
