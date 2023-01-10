#include <iostream>
#include <fstream>
#include <locale>
#include <string>
#include <list>
#include <codecvt>
#include <thread>
#include <future>

void grep(std::list<std::string> filenames, std::wstring word, std::promise<unsigned int>& len_promise) {
    unsigned int count = 0;
    for (const auto& filename : filenames) {
        std::wfstream file(filename);
        std::locale loc("pl_PL.UTF-8");
        file.imbue(loc);
        std::wstring line;
        while (getline(file, line)) {
            for (auto pos = line.find(word,0);
                 pos != std::string::npos;
                 pos = line.find(word, pos+1))
                count++;
        }
    }
    len_promise.set_value(count);
}

int main() {
    std::ios::sync_with_stdio(false);
    std::locale loc("pl_PL.UTF-8");
    std::wcout.imbue(loc);
    std::wcin.imbue(loc);

    std::wstring word;
    std::getline(std::wcin, word);

    std::wstring s_file_count;
    std::getline(std::wcin, s_file_count);
    int file_count = std::stoi(s_file_count);

    std::list<std::string> filenames1 {};
    std::list<std::string> filenames2 {};
    std::list<std::string> filenames3 {};

    std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;

    for (int file_num = 0; file_num < file_count; file_num++) {
        std::wstring w_filename;
        std::getline(std::wcin, w_filename);
        std::string s_filename = converter.to_bytes(w_filename);

        if (file_num % 3 == 0) {
            filenames1.push_back(s_filename);
        }
        else if (file_num % 3 == 1) {
            filenames2.push_back(s_filename);
        }
        else {
            filenames3.push_back(s_filename);
        }
    }


    std::promise<unsigned int> len_promise1, len_promise2, len_promise3;
    std::future<unsigned int> len_future1 = len_promise1.get_future();
    std::future<unsigned int> len_future2 = len_promise2.get_future();
    std::future<unsigned int> len_future3 = len_promise3.get_future();

    std::thread t1{
            [filenames1, word, &len_promise1]{
                grep(filenames1, word, len_promise1);
            }
    };
    std::thread t2{
            [filenames2, word, &len_promise2]{
                grep(filenames2, word, len_promise2);
            }
    };
    std::thread t3{
            [filenames3, word, &len_promise3]{
                grep(filenames3, word, len_promise3);
            }
    };

    unsigned int len1 { 0 }, len2 { 0 }, len3 { 0 };
    len1 = len_future1.get();
    len2 = len_future2.get();
    len3 = len_future3.get();

    unsigned int count = len1 + len2 + len3;

    t1.join();
    t2.join();
    t3.join();

    std::wcout << count << std::endl;
}