/*
MIT License
Copyright(c) 2020 Futurewei Cloud
<<<<<<< HEAD
<<<<<<< HEAD

=======
>>>>>>> ebbbddd (Update code)
=======
>>>>>>> 023aabe85b7ba0be97f55680985ddcf5da746190
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:
<<<<<<< HEAD
<<<<<<< HEAD

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

=======
    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>>>>>>> ebbbddd (Update code)
=======
    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>>>>>>> 023aabe85b7ba0be97f55680985ddcf5da746190
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.route.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.CONFLICT, reason="destinations are same")
public class DestinationSame extends Exception {

    public DestinationSame () {}

    public DestinationSame (String message) {
        super(message);
    }
}
