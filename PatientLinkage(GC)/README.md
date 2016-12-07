Secure Record Linkage
====

Requirement
---
JDK8 must be installed to run this program.

Run the program
---
The two parties are called as geneator and evaluator in garbled circuit protocol. To run the GB protocol, both generator and evaluator need to specify the of their config file (see the specis of configure file in the following) and data file.

To start the run the record linkage program, enter into the program folder, and start one party (generator or evaluator) program:
```
>>java -jar dist/PatientLinkageGC.jar -config <config file> -data <data file>
```

Example
---
The geneator has a 1000 records in file "Source14k_a_1K.csv", and its configure file is "config_gen_1K.txt"; the evaluator has a 1000 records in file "Source14k_b_1K.csv", and its configure file is "config_eva_1K.txt".

In the generator side:
```
>>java -jar dist/PatientLinkageGC.jar -config configs/config_gen_1K.txt -data data/Source14k_a_1K.csv
```

In the evaluator side:
```
>>java -jar dist/PatientLinkageGC.jar -config configs/config_eva_1K.txt -data data/Source14k_b_1K.csv
```

Configure file specs
---
Words after "|" in each line are comments.

**party:**
the role of the program, it can be either “generator” or “evaluator”.

**address:**
the generator address.

**port:**
the generator port. Note, for multiple threads computation, the same number of consecutive ports starting from this port will be occupied for communications.

**threshold:**
matching threshold from the party.

**threads:**
computation thread number, and both party must have the same thread number.

**records # of the opposite party:**
as it specifies.

**filter hash bits:**
the bit number of hashes of the rules.

**results save path:**
the results will be saved is this file.

**rule:**
rule stands for the criterion (combination of properties) for matching. Here, the numbers in the left of ->  are character lengths of corresponding property, and the number in the right is the weight of this rule. For example, ‚”3  0  6 -> 1” means the rule contain 3 characters of 1st property, 6 characters of 3rd property, and the rule weight is 1. Note, if the character number of the property contains “S”,  this property will be encoded by soundex method, and the number immediate after S means concatenating with first length of this property. For example, “S3” means encode the property by soundex + the first three character of this property. If the weight is 0, then the garbled circuit will declare a match of two records if one of these rules from the two records are equal.

Contact
---
If you have any question or bug report, feel free to email me at *f4chen@ucsd.edu*.
