#include <iostream>
#include <fstream>
#include <locale>
#include <string>
#include <list>
#include <codecvt>
#include <thread>
#include <future>
#include <vector>

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
    std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;

    auto grep = [](const std::vector<std::list<std::string>>& filenames, const std::wstring& word, std::promise<unsigned int> len_promises, int i){
        unsigned int count = 0;
        for (const auto& filename : filenames[i]) {
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
        len_promises.set_value(count);
    };

    int nr = 3;
    unsigned int count = 0;
    std::vector<std::list<std::string>> filenames(nr);
    std::vector<std::future<unsigned int>> len_futures;
    std::vector<std::thread> threads;

    for (int file_num = 0; file_num < file_count; file_num++) {
        std::wstring w_filename;
        std::getline(std::wcin, w_filename);
        std::string s_filename = converter.to_bytes(w_filename);
        filenames[file_num % nr].push_back(s_filename);
    }

    for (int i = 0; i < nr; i++) {
        std::promise<unsigned int> promise;
        len_futures.push_back(promise.get_future());
        threads.emplace_back(grep, filenames, word, std::move(promise), i);
    }

    for (int i = 0; i < nr; i++) {
        count += len_futures[i].get();
    }

    for (int i = 0; i < nr; i++) {
        threads[i].join();
    }

    std::wcout << count << std::endl;
}