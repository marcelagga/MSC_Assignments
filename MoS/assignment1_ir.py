from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
from xml.etree import ElementTree
from os import listdir
from os.path import isfile, join
from collections import Counter,defaultdict
from math import sqrt
from numpy import log,mean
import csv
import time
import sys

stop_words = list(stopwords.words('english'))

class TextProcessor:

    def __init__(self, path):
        self.path = path

    def get_path_files_names(self,path):
        """
        Returns a list of all XML files for a given path.
        """
        return [f for f in listdir(path) if ((isfile(join(path, f))) & (f.split('.')[1] == 'xml'))]


    def return_valid_text(self,text):
        """
        This method is used to avoid errors for those documents
        from the collection where TEXT or HEADLINE are missing.
        If the content is empty (None), it returns an empty string.
        """
        return text if text != None else ''


    def process_xml_document(self,PATH,document_name):
        """
        This method is used only for documents from the collection.
        Given a document, returns the content of HEADLINE and TEXT tags
        together as a string.
        """
        tree = ElementTree.parse(PATH + document_name)
        root = tree.getroot()
        headline = self.return_valid_text(root.find("HEADLINE").text)
        text = self.return_valid_text(root.find("TEXT").text)
        return headline + text


    def process_xml_query(self,PATH, document_name):
        """
        This method is used only for topics.
        Given a topic, returns a dictionary with the content of
        QUERYID, TITLE,and DESC.
        """
        tree = ElementTree.parse(PATH + document_name)
        root = tree.getroot()
        return {"QUERYID": root.find("QUERYID").text,
                "TITLE": root.find("TITLE").text,
                "DESC": root.find("DESC").text}


    def check_if_word_is_valid(self,word):
        """
        Validates if a word is considered valid by checking
        if it has characters that are numbers or symbols,
        or if the word is a stopword.
        """
        return not (any([c.isdigit() for c in word]) or
                    any(not c.isalnum() for c in word) or
                    (word in stop_words))


    def process_text(self,file_text):
        """
        It pre-processes a text by:
           1. splitting the text in words
           2. checking if the word is valid
           3. lowercasing
           4. stemming
        Returns a dictionary with the word counts for the pre-processed text
        """
        ps = PorterStemmer()
        word_tokens = word_tokenize(file_text)
        return Counter([ps.stem(w.lower()) for w in word_tokens if self.check_if_word_is_valid(w)])


    def get_docs_processed(self):
        """
        Pre-processing of all documents from a given path.
        Returns a dictionary having keys as doc_id and the
        pre-processed documents as values.
        """
        print("Starting Documents Processing...")
        start = time.time()
        collection_files = self.get_path_files_names(self.path)
        documents_processed = {}
        for file in collection_files:
            file_name = file.split('.')[0]
            file_text = self.process_xml_document(self.path,file)
            if file_text != '':
                documents_processed[file_name] = self.process_text(file_text)
        end = time.time()
        print(f'Documents Processing Total Execution Time: {round(end - start, 2)} s')
        return documents_processed

class InformationRetrieval:

    def __init__(self,docs_processed):
        self.docs_processed = docs_processed
        self.inverted_index = None
        self.length_docs = None
        self.tf_idf_collection = None

    def calculate_inverted_index(self):
        """
        Computes the inverted index matrix for a given collection of documents.
        Returns a dictionary with all words from the collection as values
        and a dictionary with documents and word frequency as values.
        """
        inverted_index = {}
        for doc in self.docs_processed:
            for word in self.docs_processed[doc]:
                if word not in inverted_index:
                    inverted_index[word] = {}
                inverted_index[word][doc] = self.docs_processed[doc][word]
        self.inverted_index = inverted_index

    def get_length_doc(self,doc_id):
        """
        Returns the number of words (length) in a pre-processed text.
        """
        return sum(self.docs_processed[doc_id].values())

    def calculate_length_docs(self):
        """
        Computes the number of words (length) for the whole collection of documents.
        Returns a dictionary with doc_id as keys and length of the document as values.
        """
        self.length_docs = {doc_id: self.get_length_doc(doc_id) for doc_id in self.docs_processed}

    def get_docs_containing_word(self,word):
        """
        Returns the documents from the collection that contain a specific word.
        """
        return self.inverted_index[word] if word in self.inverted_index else []

    def get_method_function(self,method):
        """
        Selects the similarity function for a given method
        """
        if method == 'VSM':
            return self.calculate_cosine_similarity
        elif method == 'BM25':
            return self.calculate_similarity_BM25
        else:
            return self.calculate_similarity_LM

    def get_relevant_documents(self,query_processed,method):
        """
        For a given query and method, this function returns the 1000 most relevant documents
        of the collection ordered by their similarity.
        It only considers documents that contain at least one of the words from the query. All
        other documents are excluded.
        """
        query_processed = {word: query_processed[word] for word in query_processed if word in self.inverted_index}
        similarity = self.get_method_function(method)
        all_relevant_docs = {doc for word in query_processed for doc in self.get_docs_containing_word(word)}
        documents_with_scores = {doc_id: similarity(query_processed,doc_id) for doc_id in all_relevant_docs}
        documents_with_scores = {k: v for k, v in sorted(documents_with_scores.items(),
                                                         key=lambda item: item[1], reverse=True)}
        return [(k, v) for k, v in documents_with_scores.items()][:1000]

    def get_frequency_word(self,word,doc_id):
        """
        For a given word and doc_id, returns the term frequency of the word
        in the document doc_id.
        """
        return self.inverted_index[word][doc_id] if doc_id in self.inverted_index[word] else 0

    def calculate_tf_idf(self,doc_processed=None,doc_id =None):
        """
        For a given pre-processed document or doc_id, returns a dictionary with
        the tf-idf weights for that document.
        """
        N = len(self.docs_processed)

        if doc_id != None:
            length_doc = self.length_docs[doc_id]
            doc_processed = self.docs_processed[doc_id]
        else:
            length_doc = sum(doc_processed.values())

        tf_idf = {}

        for word in doc_processed:
            tf = doc_processed[word]
            n = len(self.inverted_index[word])
            tf_idf[word] =(tf/length_doc)*log(N/n)

        return tf_idf

    def calculate_tf_idf_collection(self):
        """
        Used only for method 'VSM'.
        Computes tf-idf for the whole collection. Caching these values saves
        time when executing it for multiple queries.
        """
        self.tf_idf_collection = {doc_id:self.calculate_tf_idf(doc_id=doc_id) for doc_id in self.docs_processed}

    def calculate_norm(self,vector):
        """
        Returns the Euclidean norm for a given vector.
        """
        return sqrt(sum([v ** 2 for v in vector.values()]))

    def calculate_cosine_similarity(self,query_processed,doc_id):
        """
        Computing the tf-idf weighting for the document and query and
        returns their cosine similarity.
        """
        doc_tf_idf = self.tf_idf_collection[doc_id]
        query_tf_idf = self.calculate_tf_idf(doc_processed=query_processed)
        norm_doc = self.calculate_norm(doc_tf_idf)
        norm_query = self.calculate_norm(query_tf_idf)
        dot_product = 0
        for word in query_tf_idf:
            if doc_id in self.inverted_index[word]:
                dot_product += doc_tf_idf[word] * query_tf_idf[word]
        return dot_product / (norm_doc * norm_query)

    def calculate_similarity_BM25(self,query_processed,doc_id, k=1.5, b=0.6):
        """
        Returns the BM25 weight of a query and document from the collection
        """
        doc_processed = self.docs_processed[doc_id]
        N = len(self.docs_processed)
        dl = self.length_docs[doc_id]
        ql = sum(query_processed.values())
        avdl = mean(list(self.length_docs.values()))
        final_score = 0
        for word in query_processed:
            if word in self.inverted_index:
                n = len(self.inverted_index[word])
                tf = self.get_frequency_word(word, doc_id)
                final_score += (tf / (k * ((1 - b) + b * (dl / avdl)) + tf)) * (log((N - n - 0.5) / (n + 0.5)))
        return final_score

    def calculate_similarity_LM(self,query_processed,doc_id,lambda_value=0.7):
        """
        Returns the JMS weight of a query and document from the collection
        """
        final_score = 0
        Dl = sum(self.length_docs.values())
        dl = self.length_docs[doc_id]
        for word in query_processed:
            if word in self.inverted_index:
                tf_D = sum(self.inverted_index[word].values())
                tf_d = self.get_frequency_word(word, doc_id)
                final_score += log((1 - lambda_value) * (tf_d / dl) + lambda_value * (tf_D / Dl))
        return final_score

def main():

    #First and second paramaters are used for the collection and topics path respectively
    collection_path = sys.argv[1]
    topics_path = sys.argv[2]

    #Other arguments are used to store the methods to compute
    methods = sys.argv[3:]
    methods = list({method for method in methods if method in ['LM','VSM','BM25']})

    #Of none of the methods matches the keywords, prints an error message and stops execution
    if len(methods) == 0:
        print("None of the methods you have entered are correct. Please try using VSM, LM, or BM25")
        return 0

    #Initialises a TextProcessor
    tp = TextProcessor(collection_path)
    #Processes all the collection documents ~240 seconds
    docs_processed = tp.get_docs_processed()
    topics = tp.get_path_files_names(topics_path)
    #Initialises a InformationRetrieval for the given collection
    ir = InformationRetrieval(docs_processed)
    #Computes inverted index and length of all documents
    ir.calculate_inverted_index()
    ir.calculate_length_docs()
    #If one of the methods is VSM, precomputes the tf-idf for that collection
    if 'VSM' in methods:
        ir.calculate_tf_idf_collection()

    #For each of the given methods and queries, prints the results in the expected format of trec_eval.
    for method in methods:
        start = time.time()
        print(f"Starting evaluation for IR: {method}...")
        file_name = 'evaluation_' + method + '.txt'
        with open(file_name, mode='w') as evaluation_file:
            evaluation_writer = csv.writer(evaluation_file, delimiter=' ', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            for topic in topics:
                start_topic = time.time()
                query_xml = tp.process_xml_query(topics_path, topic)
                query_text = query_xml['TITLE'] + query_xml['DESC']
                query_processed = tp.process_text(query_text)
                relevant_docs = ir.get_relevant_documents(query_processed,method)
                for doc in relevant_docs:
                    evaluation_writer.writerow([query_xml['QUERYID'], 0, doc[0], 'rank', doc[1], 'STANDARD'])
                end_topic = time.time()
                print(f'{method} Topic Evaluation Execution Time {topic}: {round(end_topic - start_topic, 2)}s')
            end = time.time()
            print(f'{method} Total Topics Evaluation Total Execution Time: {round(end - start, 2)} s')

if __name__ == "__main__":
    main()
