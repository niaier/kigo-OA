import request from '@/utils/request'
export function upload (formData) {
  return request({
    url: '/upload/uploadFile',
    method: 'post',
    data: formData
  })
}
