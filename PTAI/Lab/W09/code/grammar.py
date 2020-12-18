# Taken from PonyGE
# Copyright (c) 2009-2012 Erik Hemberg and James McDermott
# Hereby licensed under the GNU GPL v3.
# http://ponyge.googlecode.com

import sys, copy, re, random, math, operator, pprint

class Grammar(object):
    """Context Free Grammar"""
    NT = "NT" # Non Terminal
    T = "T" # Terminal

    def __init__(self, terminals=None, non_terminals=None,
                 rules=None, start_rule=None,
                 file_name=None, input_s=None):
        """Pass either a filename or an input string, or the terminals,
        non-terminals, rules, and start rule."""
        if (terminals is not None or non_terminals is not None or
            rules is not None or start_rule is not None):

            # if we have any of these, we must have all, and we must
            # not have input_s or file_name.
            assert (non_terminals is not None and non_terminals is not None and
                    rules is not None and start_rule is not None)
            assert input_s is None and file_name is None
            self.terminals = terminals
            self.non_terminals = non_terminals
            self.rules = rules
            self.start_rule = start_rule

        else:
            if input_s is None and file_name is not None:
                input_s = open(file_name, 'r').read()
            self.parse_bnf(input_s)
        
    def parse_bnf(self, input_s):
        """Parse a grammar in BNF format, passed in as a string."""

        self.rules = {}
        self.non_terminals, self.terminals = set(), set()
        self.start_rule = None
        
        rule_separator = "::="
        # Don't allow space in NTs, and use lookbehind to match "<"
        # and ">" only if not preceded by backslash. Group the whole
        # thing with capturing parentheses so that split() will return
        # all NTs and Ts. TODO does this handle quoted NT symbols?
        non_terminal_pattern = r"((?<!\\)<\S+?(?<!\\)>)"
        # Use lookbehind again to match "|" only if not preceded by
        # backslash. Don't group, so split() will return only the
        # productions, not the separators.
        production_separator = r"(?<!\\)\|"

        # Parse the grammar
        for line in input_s.split("\n"):
            if not line.startswith("#") and line.strip() != "":
                # Split rules. Everything must be on one line
                if line.find(rule_separator):
                    lhs, productions = line.split(rule_separator, 1) # 1 split
                    lhs = lhs.strip()
                    if not re.search(non_terminal_pattern, lhs):
                        raise ValueError("lhs is not a NT:", lhs)
                    self.non_terminals.add(lhs)
                    if self.start_rule == None:
                        self.start_rule = lhs
                    # Find terminals and non-terminals
                    tmp_productions = []

                    # Usual case: iterate through productions on RHS
                    for production in re.split(production_separator, productions):
                        production = production.strip().replace(r"\|", "|")
                        tmp_production = []
                        for symbol in re.split(non_terminal_pattern, production):
                            symbol = symbol.replace(r"\<", "<").replace(r"\>", ">")
                            if len(symbol) == 0:
                                continue
                            elif re.match(non_terminal_pattern, symbol):
                                tmp_production.append(symbol)
                            else:
                                self.terminals.add(symbol)
                                tmp_production.append(symbol)

                        tmp_productions.append(tmp_production)
                    # Create a rule
                    if not lhs in self.rules:
                        self.rules[lhs] = tmp_productions
                    else:
                        raise ValueError("lhs should be unique", lhs)
                else:
                    raise ValueError("Each rule must be on one line")
                
        assert self.start_rule is not None
        
    def __repr__(self):
        return "Grammar(terminals=%s, non_terminals=%s, rules=%s, start_rule='%s')" % (
            self.terminals, self.non_terminals, self.rules, self.start_rule)
    
    def derive_string(self, s=None):
        """Recursively derive a string. Don't create the derivation tree."""
        if s is None:
            s = self.start_rule
        elif s in self.terminals:
            return s
        rule = self.rules[s]
        idx = random.randrange(len(rule))
        prod = rule[idx]
        return "".join([self.derive_string(s) for s in prod])    
