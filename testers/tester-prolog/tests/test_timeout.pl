:- use_module('eplunit').

recursive(0).
recursive(N) :-
  M is N - 1,
  recursive(M).

:- begin_tests(timeout).

test(tst_success) :- recursive(10).
test(tst_timeout) :- recursive(-1).
test(tst_shorter_timeout, [timeout(0.1)]) :- recursive(-1).
test(tst_shorter_timeout, [timeout(0.1), throws(time_limit_exceeded)]) :- recursive(-1).
% test(tst_shorter_timeout, [timeout(0)]) :- recursive(-1). % Domain error
% test(tst_shorter_timeout, [timeout(-1)]) :- recursive(-1). % Domain error

:- end_tests(timeout).
