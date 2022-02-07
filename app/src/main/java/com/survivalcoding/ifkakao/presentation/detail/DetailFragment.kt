package com.survivalcoding.ifkakao.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayoutMediator
import com.survivalcoding.ifkakao.R
import com.survivalcoding.ifkakao.databinding.FragmentDetailBinding
import com.survivalcoding.ifkakao.domain.model.Session
import com.survivalcoding.ifkakao.presentation.adapter.SessionListAdapter
import com.survivalcoding.ifkakao.presentation.detail.viewpager.DetailPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private lateinit var player: ExoPlayer
    private val adapter by lazy {
        SessionListAdapter { session -> moveToDetail(session) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.get("sessionId") as Int?)?.run { viewModel.getSessionById(this) }

        player = ExoPlayer.Builder(requireContext()).build()
        binding.detailPlayerView.player = player
        viewModel.session.observe(viewLifecycleOwner) { session ->
            val mediaItem = MediaItem.fromUri(session.linkList.VIDEO[0].url)

            // sample from https://stickode.tistory.com/165
//            val mediaItem =
//                MediaItem.fromUri("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4")
            val mediaSource = ProgressiveMediaSource.Factory(
                DefaultDataSourceFactory(
                    requireContext(),
                    Util.getUserAgent(requireContext(), requireContext().applicationInfo.name)
                )
            ).createMediaSource(mediaItem)
            player.setMediaSource(mediaSource)
            player.prepare()

            binding.detailViewPager.adapter = DetailPagerAdapter(this, session)

            TabLayoutMediator(binding.detailTabLayout, binding.detailViewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "세션 설명"
                    1 -> "댓글"
                    else -> throw Exception()
                }
            }.attach()

            // 연관 세션 조회
            viewModel.getSessionsRelated(session.idx, session.field)
        }

        // 연관 세션 recyclerView 설정
        binding.detailRvRelation.adapter = adapter

        viewModel.relatedSessions.observe(this) { sessions ->
            adapter.submitList(sessions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
    }

    private fun moveToDetail(session: Session?) {
        parentFragmentManager.commit {
            replace<DetailFragment>(
                R.id.fragment_container_view,
                args = bundleOf("sessionId" to session?.idx),
            )
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }
}