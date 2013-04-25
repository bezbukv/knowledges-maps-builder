#-*- coding: utf-8 -*-
""" Нужно получить все
    
    thinks : walk and regex
    
    http://docs.python.org/2/tutorial/errors.html
"""
import os
 
class CrawlerException(Exception):
    def __init__(self, value):
        self.value = value
        
    def __str__(self):
        return str(self.value)

def _check_extension(string, extension_list):
    """ может быть ошибка, хотя маловероятна. Точка вероятность повышает 
    
    TODO(zaqwes): проверка заперщенных расширений
    """
    for k in extension_list:
        if '.'+k == string[-len(k)-1:].lower():
            return True
    return False

def find_files_down_tree(root, extension_list, ignored_dirs=None):
    """ Получить список файлов заданных типов с полными путями

    Args:
        ignoreList
            1. пути - папки
            2. расширения, которые похожи на разрешенные
            3. целые файлы (с путем(1 шт) и без(может быть много))
            4. регулярные выражения - подстроки
        
    troubles testing :
        разные типы данных - возвр. знач. и сообщение - но нужно 
            принимать из функции два значения
    """
    
    def on_error_walk(err):      
        # TODO: сделать свой класс обработки ошибок
        raise CrawlerException(err)
    
    result_list = list('')
    # получаем объект для обхода
    # Если корня нет исключение генерируется при доступе
    try:
        getted_list = os.walk(root, onerror=on_error_walk)
        for root, dirs, files in getted_list:
            if files:
                for name in files:
                    if _check_extension(name, extension_list):
                        bResult = True
                        if ignored_dirs:
                            for it in ignored_dirs:
                                if it in root:
                                    bResult = False
                            
                        if bResult:
                            slash = '\\'
                            full_path = root+slash+name
                            result_list.append(unicode(str(unicode(str(full_path), 'cp1251')), 'utf8'))
    except CrawlerException as e:
        return None, (1, str(e))

    # возвращаем что насобирали
    return result_list, (0, '')    # может в питоне и нече, но вообеще-то...?
        # вобщем нужно подумать над обработкой ошибок


""" How use it """
if __name__ == '__main__':
    root = 'D:/doc_pdf_odt'
    extension_list = ['pdf']
    
    # поиск
    result_list, err = find_files_down_tree(root, extension_list)
    
    def printer(msg):
        print msg

    map(printer, result_list)
    
    print 'Done'



