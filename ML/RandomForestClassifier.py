import pandas as pd
import numpy as np
import random
from collections import Counter

def gini(y):
    if len(y)==0: return 0
    _,counts = np.unique(y,return_counts=True)
    probs = counts/counts.sum()
    gini = np.sum(probs*probs)
    return 1-gini

class Stump:

    def __init__(self,y,feature,value,number_of_classes):
        self.y = y
        self.feature = feature
        self.value = value
        self.number_of_classes = number_of_classes

    def count_elements(self):
        count_elements = dict(Counter(self.y))
        return [count_elements.get(n,0) for n in range(0,self.number_of_classes)]


class TreeClassifier:

    def __init__(self,loss_function = gini,min_samples_split = 2,max_depth = 6, count = 0,forest_flag = 0):

        self.loss_function = loss_function
        self.max_depth     = max_depth
        self.count         = count
        self.forest_flag   = forest_flag
        self.min_samples_split = min_samples_split
        self.features      = None
        self.number_of_classes = None
        self.tree          = None

    def fit(self,X,y,count = 0):

        if(count == 0):
            self.features = range(X.shape[1])
            self.number_of_classes = np.unique(y).shape[0]

        if ((np.abs(self.loss_function(y)) < 1e-5) or (count == self.max_depth) or (len(y) < self.min_samples_split)):
            return Stump(y,None,None,self.number_of_classes)

        best_feature,best_value,left_idxs,right_idxs = self.find_best_decision(X,y)

        if len(left_idxs) == 0:
            return Stump(y[right_idxs],None,None,self.number_of_classes)

        elif len(right_idxs) == 0:
            return Stump(y[left_idxs],None,None,self.number_of_classes)

        else:
            stump = Stump(y,best_feature,best_value,self.number_of_classes)
            sub_tree = {stump:[]}
            left_branch  = self.fit(X[left_idxs,:],y[left_idxs],count+1)
            right_branch  = self.fit(X[right_idxs,:],y[right_idxs],count+1)
            sub_tree[stump].append(left_branch)
            sub_tree[stump].append(right_branch)
            self.tree = sub_tree
            return sub_tree

    def predict(self,X):

        prediction = []

        if self.tree == None:
            print('This tree has not been fit yet')
            return -1

        else:
            for example in X:
                prediction.append(self.predict_example(example,self.tree))
            return np.array(prediction)

    def predict_example(self,example,tree):

        stump = list(tree.keys())[0]
        feature_name = stump.feature
        value   = stump.value

        if example[feature_name] <= value:
            next_branch = tree[stump][0]
        else:
            next_branch = tree[stump][1]

        if not isinstance(next_branch, dict):
            classes_stump = next_branch.count_elements(self)
            return stump.index(max(classes_stump))
        else:
            return self.predict_example(example, next_branch)


    def predict_proba(self,X):
        prediction = []

        if self.tree == None:
            print('This tree has not been fit yet')
            return -1

        else:
            for row in X:
                prediction.append(self.predict_example_proba(row,self.tree))
            return np.array(prediction)

    def predict_example_proba(self,example,tree):

        stump = list(tree.keys())[0]
        feature_name = stump.feature
        value   = stump.value

        if example[feature_name] <= value:
            next_branch = tree[stump][0]
        else:
            next_branch = tree[stump][1]

        if not isinstance(next_branch, dict):
            classes_stump = np.array(next_branch.count_elements())
            return classes_stump/np.sum(classes_stump)
        else:
            return self.predict_example_proba(example, next_branch)


    def split_exemples(self,X,feature,value):
        return {'L':np.where(X[:,feature] <= value)[0],'R':np.where(X[:,feature] > value)[0]}

    def get_values_splits(self,X):

        feature_values = np.sort(np.unique(X))

        if len(feature_values) == 1:
            return [feature_values[0]]
        else:
            return [(feature_values[n]+feature_values[n+1])/2 for n in range(len(feature_values)-1)]

    def find_best_split(self,X,y,feature):

        values_splits  = self.get_values_splits(X[:,feature])
        total_values  = X.shape[0]
        minimum_loss  = np.inf

        for value in values_splits:

            split_examples = self.split_exemples(X,feature,value)
            left_idxs = split_examples['L']
            right_idxs = split_examples['R']
            Lprob = len(left_idxs)/total_values
            Rprob = len(right_idxs)/total_values
            Lloss = self.loss_function(y[left_idxs])
            Rloss = self.loss_function(y[right_idxs])

            loss_split = Lprob*Lloss+Rprob*Rloss

            if loss_split < minimum_loss:
                minimum_loss = loss_split
                results = [value,minimum_loss,left_idxs,right_idxs]

        return results

    def find_best_decision(self,X,y):

        lowest_loss = np.inf

        if self.forest_flag == 1:
            features = np.random.choice(self.features,int(round(np.sqrt(X.shape[1]))),replace=False)

        for feature in features:

            best_value,minimum_loss,left_idxs,right_idxs = self.find_best_split(X,y,feature)

            if  minimum_loss < lowest_loss:
                lowest_loss = minimum_loss
                result = [feature,best_value,left_idxs,right_idxs]

        return result

class RandomForestClassifier:

    def __init__(self,n_estimators = 100, sample_size = None,loss_function = gini, min_samples_split = 2,verbose = 0,bootstrap = True):
        self.n_estimators = n_estimators
        self.sample_size = sample_size
        self.min_samples_split = min_samples_split
        self.trees = None
        self.loss_function = loss_function
        self.verbose = verbose
        self.bootstrap = bootstrap

    def predict(self, X):
        probs = self.predict_proba(X)
        return np.argmax(probs, axis=1)

    def predict_proba(self, X):
        return np.round(np.mean([t.predict_proba(X) for t in self.trees], axis=0),2)


    def fit(self, X,y):

        if(self.verbose == 1):
            print(f'Tree number {k+1}')

        if self.sample_size == None:
            self.sample_size = X.shape[0]

        self.trees = [TreeClassifier(self.loss_function,min_samples_split=self.min_samples_split,max_depth = -1,forest_flag = 1) for i in range(self.n_estimators)]

        for rd,t in enumerate(self.trees):
            if self.bootstrap:
                rnd_idxs = random.choices(range(X.shape[0]), k=self.sample_size)
            else:
                rnd_idxs = range(X.shape[0])

            t.fit(X[rnd_idxs],y[rnd_idxs])
