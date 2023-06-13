
#include <iostream>
#include <queue>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <vector>


static std::queue<std::vector<float>> queue_;
static std::mutex mutex_;
static std::condition_variable condition_;

void send(const std::vector<float> &value) {
    std::unique_lock<std::mutex> lock(mutex_);
    queue_.push(value);
    lock.unlock();
    condition_.notify_one();
}

std::vector<float> receive() {
    std::unique_lock<std::mutex> lock(mutex_);
    condition_.wait(lock, []() { return !queue_.empty(); });
    std::vector<float> value = queue_.front();
    queue_.pop();
    return value;
}







//class ChannelVectorFloat {
//public:
//    void send(const std::vector<float>& value) {
//        std::unique_lock<std::mutex> lock(mutex_);
//        queue_.push(value);
//        lock.unlock();
//        condition_.notify_one();
//    }
//
//    std::vector<float> receive() {
//        std::unique_lock<std::mutex> lock(mutex_);
//        condition_.wait(lock, [this]() { return !queue_.empty(); });
//        std::vector<float> value = queue_.front();
//        queue_.pop();
//        return value;
//    }
//
//private:
//    std::queue<std::vector<float>> queue_;
//    std::mutex mutex_;
//    std::condition_variable condition_;
//};
//
//
//template<typename T>
//class Channel {
//public:
//    void send(const T& value) {
//        std::unique_lock<std::mutex> lock(mutex_);
//        queue_.push(value);
//        lock.unlock();
//        condition_.notify_one();
//    }
//
//    T receive() {
//        std::unique_lock<std::mutex> lock(mutex_);
//        condition_.wait(lock, [this]() { return !queue_.empty(); });
//        T value = queue_.front();
//        queue_.pop();
//        return value;
//    }
//
//private:
//    std::queue<T> queue_;
//    std::mutex mutex_;
//    std::condition_variable condition_;
//};


