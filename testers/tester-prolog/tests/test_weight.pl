:- use_module('eplunit').

:- begin_tests(weight).

test(weight_2, [weight(2)]) :- true.
test(weight_3, [weight(3)]) :- true.
test(weight_2_fail, [weight(2)]) :- fail.
test(weight_0, [weight(0)]) :- true.

% test(weight_invalid, weight(1.5)) :- fail. % Valid weights are only nonnegative integers
% test(weight_invalid, weight(-1)) :- fail. % Valid weights are only nonnegative integers

:- end_tests(weight).
